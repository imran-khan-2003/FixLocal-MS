import { MapContainer, Marker, Polyline, Popup, TileLayer, useMap } from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import { useEffect, useMemo } from "react";

const DEFAULT_ZOOM = 13;

const icon = new L.Icon({
  iconUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png",
  iconRetinaUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png",
  shadowUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png",
  iconSize: [25, 41],
  iconAnchor: [12, 41],
});

function Recenter({ position }) {
  const map = useMap();
  useEffect(() => {
    if (position) {
      map.setView(position, map.getZoom(), { animate: true });
    }
  }, [map, position]);
  return null;
}

function LiveLocationMap({ location, userAddress, stale, destination }) {
  if (!location?.latitude || !location?.longitude) {
    return (
      <div className="border border-slate-200 rounded-2xl p-4 text-sm text-slate-600 bg-slate-50">
        Awaiting live location updates from the tradesperson.
      </div>
    );
  }

  const position = [location.latitude, location.longitude];
  const hasDestination =
    destination && destination.latitude && destination.longitude;
  const routePoints = useMemo(() => {
    if (!hasDestination || !location) return [];
    return [
      [location.latitude, location.longitude],
      [destination.latitude, destination.longitude],
    ];
  }, [destination, hasDestination, location]);

  return (
    <div className="border border-slate-200 rounded-2xl overflow-hidden shadow-sm">
      <MapContainer center={position} zoom={DEFAULT_ZOOM} style={{ height: "320px", width: "100%" }}>
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        <Marker position={position} icon={icon}>
          <Popup>
            <div className="space-y-1">
              <p className="font-semibold">Tradesperson location</p>
              {stale ? (
                <p className="text-xs text-amber-600">Last update looks stale.</p>
              ) : (
                <p className="text-xs text-emerald-600">Live update received.</p>
              )}
            </div>
          </Popup>
        </Marker>
        {hasDestination && (
          <Marker
            position={[destination.latitude, destination.longitude]}
            icon={icon}
          >
            <Popup>
              <div className="space-y-1">
                <p className="font-semibold">User destination</p>
                <p className="text-xs text-slate-500">{userAddress || destination.label}</p>
              </div>
            </Popup>
          </Marker>
        )}
        {routePoints.length === 2 && (
          <Polyline positions={routePoints} color="#2563eb" weight={4} opacity={0.7} />
        )}
        <Recenter position={position} />
      </MapContainer>
      <div className="px-4 py-3 text-xs text-slate-600 bg-white border-t border-slate-100">
        <p className="font-semibold text-slate-700">Service address</p>
        <p>{userAddress || "Not provided"}</p>
      </div>
    </div>
  );
}

export default LiveLocationMap;