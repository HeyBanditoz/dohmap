const greyIcon = new L.Icon({
    iconUrl: "/static/leaflet/images/marker-icon-grey.png",
    iconRetinaUrl: "/static/leaflet/images/marker-icon-2x-grey.png",
    shadowUrl: "/static/leaflet/images/marker-shadow.png",
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    tooltipAnchor: [16, -28],
    shadowSize: [41, 41]
})
const baseIcon = L.marker().getIcon(); // TODO surely the default icon is defined somewhere...
const markers = new Map();
let leaflet;

function setup() {
    leaflet = L.map('map').setView([40.64053, -111.934204], 11);
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
    }).addTo(leaflet);

    fetchPins()
        .then(layers => {
            $('#loading-progress-bar').hide();
            L.control.layers(null, Object.fromEntries([...layers.entries()].sort())).addTo(leaflet);
        })
}

async function fetchPins() {
    const cities = new Map();
    await fetch("/api/pins/all")
        .then(res => res.json())
        .then(data => {
            console.log(`We have ${data.data.length} markers.`);
            // should we just use JS objects here?
            data.data
                .filter(marker => marker.lat !== null)
                .forEach(marker => {
                    // drop ' TS' from markers to group together those cities, i.e. 'MAGNA' and 'MAGNA TS'
                    if (marker.establishment.city.endsWith(' TS')) {
                        marker.establishment.city = marker.establishment.city.replace(' TS', '');
                    }
                    if (!cities.has(marker.establishment.city)) {
                        cities.set(marker.establishment.city, L.layerGroup());
                    }
                    const lMarker = L.marker([marker.lat, marker.lng], {id: marker.establishment.id, icon: (marker.possiblyGone ? greyIcon : baseIcon)})
                        // .bindPopup(`<b>${marker.establishment.name} <a target="_blank" title="Open standalone page for this establishment" href="/establishment/${marker.establishment.id}">⧉</a></b><br>${marker.establishment.address}<br>${marker.lat},${marker.lng}`)
                        .addTo(cities.get(marker.establishment.city))
                        .bindTooltip(`<b class="${marker.possiblyGone ? 'gone' : ''}">${marker.establishment.name}${marker.coordinatesModified ? '*' : ''}</b><br>${marker.establishment.address}`)
                        .on('click', (pin) => handlePinClick(pin));
                    markers.set(marker.establishment.id, lMarker);
                });
        })
    return cities;
}

function handlePinClick(e) {
    fetch(`/establishment/${e.target.options.id}/fragment`, {headers: {'Accept': 'text/html'}})
        .then(res => res.text())
        .then(html => $('#inspection').html(html))
        .then(_ => Init.page())
}
