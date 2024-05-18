package io.banditoz.dohmap.scraper.utco;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import io.banditoz.dohmap.model.DataSource;
import io.banditoz.dohmap.model.Establishment;
import io.banditoz.dohmap.model.Inspection;
import io.banditoz.dohmap.service.EstablishmentService;
import io.banditoz.dohmap.service.InspectionService;
import io.banditoz.dohmap.service.ViolationService;
import io.banditoz.dohmap.utils.DateSysId;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Metrics;
import jakarta.annotation.Nullable;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UTCOHealthInspectionScraper {
    private final EstablishmentService establishmentService;
    private final InspectionService inspectionService;
    private final ViolationService violationService;
    private final Set<String> seenEstablishments;
    private final Map<String, List<DateSysId>> previousInspectionDates;
    private static final Logger log = LoggerFactory.getLogger(UTCOHealthInspectionScraper.class);
    private static final Pattern UNID_PATTERN = Pattern.compile("RestrictToCategory=(.*)");
    private static final DateTimeFormatter INSPDATE = DateTimeFormatter.ofPattern("M/d/uuuu");
    private static final RetryPolicy<Object> RETRY_POLICY = RetryPolicy
            .builder()
            .handle(Exception.class)
            .withDelay(Duration.ofSeconds(1))
            .withMaxRetries(3)
            .onFailedAttempt(event -> log.warn("Connection failed, retrying (attempt #{})", event.getAttemptCount()))
            .build();

    public UTCOHealthInspectionScraper(EstablishmentService establishmentService,
                                       InspectionService inspectionService,
                                       ViolationService violationService,
                                       Set<String> seenEstablishments,
                                       Map<String, List<DateSysId>> previousInspectionDates) {
        this.establishmentService = establishmentService;
        this.inspectionService = inspectionService;
        this.violationService = violationService;
        this.seenEstablishments = seenEstablishments;
        this.previousInspectionDates = previousInspectionDates;
    }

    public void run(String letters) throws IOException {
        log.info("scanning={}", letters); // to debug
        Elements estReults = getSearchResults(letters);
        if (estReults == null) {
            return;
        }
        for (Element estReult : estReults) {
            try {
                String url = "https://www.inspectionsonline.us" + estReult.attr("href");
                Matcher matcher = UNID_PATTERN.matcher(url);
                if (!matcher.find()) {
                    log.warn("Regex didn't match for url={}", url);
                    continue;
                }
                String unid = matcher.group(1);
                MDC.put("unid", unid);
                if (seenEstablishments != null && seenEstablishments.contains(unid)) {
                    log.debug("UNID={} was seen already this run. Skipping...", unid);
                    continue;
                } else if (seenEstablishments != null) {
                    seenEstablishments.add(unid);
                }
                Establishment.Builder builder = parseEstablishmentFromTidlizedString(getEstablishmentDetails(unid), unid);
                if (builder == null) {
                    continue;
                }
                Establishment establishment = establishmentService.getOrCreateEstablishment(builder);
                List<UTCOInspection> inspections = getInspections(url);

                for (UTCOInspection inspection : inspections) {
                    if (previousInspectionDates.getOrDefault(establishment.id(), Collections.emptyList()).contains(new DateSysId(inspection.inspectionDate(), inspection.unid()))) {
                        continue;
                    }
                    MDC.put("inspectionUnid", inspection.unid());
                    processSingleInspection(inspection, establishment); // also handles violations
                }
            } catch (Exception ex) {
                log.error("Fatal exception. Proceeding to next one...",  ex);
            } finally {
                MDC.clear();
            }
        }
    }

    private Elements getSearchResults(String letters) throws IOException {
        Element results = getSearchPage(letters);
        int count = Integer.parseInt(results.getElementsByTag("h4").getFirst().text().split(" ")[0]);
        if (count == 0) {
            log.warn("documentCount=0 found for letters={}. Skipping", letters);
            return null;
        } else if (count == 1000) {
            log.warn("documentCount=1000 found for letters={} as that's the limit. There may be missing data for these letters.", letters);
        } else {
            log.info("letters={} returned documentCount={}", letters, count);
        }
        return results.select(":root > table a[target]");
    }

    private List<UTCOInspection> getInspections(String url) throws IOException {
        Element inspectionsPage = getPage(url);
        Elements pageInsps = inspectionsPage.select(".gt").select(".row");
        pageInsps.removeFirst();
        return pageInsps.stream()
                .map(element -> new UTCOInspection(
                        element.selectFirst("a").attr("href").split("&pUNID=")[1],
                        LocalDate.parse(element.select(".column2 > a").text(), INSPDATE),
                        element.select(".column1 > a").text()))
                .toList();
    }

    private void processSingleInspection(UTCOInspection inspection, Establishment establishment) throws IOException {
        Inspection.Builder dbInspectionBuilder = inspection.toDbInspection(establishment.id());
        Inspection dbInspection = inspectionService.getOrCreateInspection(dbInspectionBuilder);

        Element violationPage = getPage(inspection.getUrlToViolations());
        Elements violations = violationPage.select("table.gt:nth-child(5) > tbody:nth-child(2) > tr:nth-child(n+3):not(:last-child)");
        for (Element violation : violations) {
            MDC.put("violationUnid", inspection.unid());
            processSingleViolation(violation, dbInspection);
        }
    }

    private void processSingleViolation(Element violation, Inspection dbInspection) {
        boolean critical = false;
        boolean correctedOnSite = false;
        String code;
        String observed = "";

        // determine critical and correctedOnSite
        Elements criticalOrCos = violation.select("tr:nth-child(2) b");
        if (criticalOrCos.size() == 1 && "CRITICAL".equals(criticalOrCos.get(0).text())) {
            critical = true;
        } else if (criticalOrCos.size() == 2) {
            if ("CRITICAL".equals(criticalOrCos.get(0).text())) {
                critical = true;
            }
            if ("Corrected On Site".equals(criticalOrCos.get(1).text())) {
                correctedOnSite = true;
            }
        }

        code = violation.select("td").select(".bS").getFirst().text();
        // TODO fix this horror
        Elements text = violation.select("td[colspan=2]");
        String obs, res;
        if (text.size() == 2) {
            // if no resolution is present
            obs = text.get(1).text();
            res = null;
        } else {
            // if resolution is present
            obs = text.get(2).text();
            res = text.get(1).text();
        }
        if (!obs.equals("OBSERVATIONS & CORRECTIVE ACTIONS:")) {
            observed = obs.replaceFirst("OBSERVATIONS & CORRECTIVE ACTIONS: ", "");
        }
        if (res != null && !res.equals("RECOMMENDED RESOLUTION:")) {
            if (!observed.isEmpty()) {
                observed += " - ";
            }
            observed += text.get(1).text().replaceFirst("RECOMMENDED RESOLUTION: ", "");
        }
        UTCOViolation utcoViolation = new UTCOViolation(code, observed, critical, correctedOnSite);
        violationService.getOrCreationViolation(utcoViolation.toDbViolation(dbInspection.id()));
    }

    private Element getSearchPage(String letters) throws IOException {
        letters = letters + '*';
        String query = "Query="
                + URLEncoder.encode("[fld_EstabName] CONTAINS \"" + letters + "\" OR [fld_EstabName] CONTAINS \"" + letters + "\") OR (([fid_FaciName] CONTAINS \"" + letters + "\" OR [fid_FaciName] CONTAINS \"" + letters + "\"))", StandardCharsets.UTF_8)
                + "&SearchOrder=4&SearchMax=0"; // max appears to be 1000
        return placeRequest("https://www.inspectionsonline.us/ut/utahprovo/Inspect.nsf/0feb630b9bb24d7d86258a61004f9d51?SearchView", query, Connection.Method.POST)
                .parse()
                .body();
    }

    private Element getPage(String url) {
        Connection.Response response = placeRequest(url, null, Connection.Method.GET);
        return Failsafe.with(RETRY_POLICY).get(() -> response.parse().body());
    }

    private String getEstablishmentDetails(String unid) {
        unid = "https://www.inspectionsonline.us/ut/utahprovo/inspect.nsf/(ag_getDocValues)?OpenAgent&xx_UNID=" + unid + "&xx_fldList=fld_EstabName~fld_FaciName~fld_FStreetNo~fld_FStreetName~fld_FCity~fld_FProv~fld_FPCode~fld_EstType~fld_SCPhone~fld_InspectionDate~fld_AllcvCounter~fld_AllvCounter~fld_EstTypeAlt~fld_RFvCountsUseVLib~fld_QRFICounts";
        Connection.Response response = placeRequest(unid, null, Connection.Method.GET);
        return Failsafe.with(RETRY_POLICY).get(response::body);
    }

    private Connection.Response placeRequest(String url, String body, Connection.Method method) {
        long before = System.nanoTime();
        Connection.Response response = Failsafe.with(RETRY_POLICY).get(() -> Jsoup.connect(url)
                .header("Accept", "text/html")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .userAgent("jsoup/1.17.2 (+banditoz@protonmail.com)") // no point in hiding it
                .method(method)
                .requestBody(body)
                .timeout(120 * 1000) // two minutes
                .execute());
        double timeTookSec = (System.nanoTime() - before) / 1_000_000_000D;
        DistributionSummary.builder("dohmap_utco_request_time")
                .tags("op", getOpFromUrl(url))
                .publishPercentiles(0.50, 0.75, 0.99).register(Metrics.globalRegistry)
                .record(timeTookSec);
        return response;
    }

    @Nullable
    private Establishment.Builder parseEstablishmentFromTidlizedString(String tString, String unid) {
        List<String> results = Arrays.stream(tString.replaceAll("\n", "").split("~"))
                .map(String::trim)
                .toList();
        if (results.size() == 3) {
            // crap data, only name ???
            // how do we get zip code from previous table here too, since that's set there but not here??? wtf???????
            // anyway, this is bad data.
            return null;
        }
        if (results.get(9).startsWith("kw_")) {
            throw new IllegalArgumentException("Field 10 was not a phone number, but was " + results.get(9));
        }
        // do some validation/logging on phone number
        if (!results.get(9).matches("(\\d\\s)?\\(\\d{3}\\) \\d{3}-\\d{4}")) {
            log.warn("Crap phone number received for unid={} phone={}", unid, results.get(9));
        }
        return new Establishment.Builder()
                .setName(results.get(1))
                .setAddress(results.get(3) + ' ' + results.get(4))
                .setCity(results.get(5))
                .setState("UT")
                .setZip(results.get(7))
                .setType(results.get(8).replaceFirst("kw_", ""))
                .setPhone(results.get(9))
                .setSource(DataSource.UTAH_COUNTY_PARAGON)
                .setSysId(unid);
    }

    // make this cleaner
    private String getOpFromUrl(String url) {
        if (url.contains("ag_dspPubDetail")) {
            return "inspectionDetails";
        } else if (url.contains("ag_getDocValues")) {
            return "establishmentDetails";
        } else if (url.contains("w_InspectionsPubSumm-NT")) {
            return "inspectionList";
        } else if (url.contains("SearchView")) {
            return "search";
        } else {
            return "unknown";
        }
    }
}
