import { MapContainer, Marker, Polyline, Popup, TileLayer, useMap } from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import { useEffect, useMemo, useState } from "react";

const DEFAULT_ZOOM = 13;
const OSRM_ENDPOINT = "https://router.project-osrm.org";

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
  const [routePoints, setRoutePoints] = useState([]);
  const [routeMeta, setRouteMeta] = useState(null);
  const [routeLoading, setRouteLoading] = useState(false);
  const [routeError, setRouteError] = useState("");

  const fallbackRoute = useMemo(() => {
    if (!hasDestination || !location) return [];
    return [
      [location.latitude, location.longitude],
      [destination.latitude, destination.longitude],
    ];
  }, [destination, hasDestination, location]);

  useEffect(() => {
    if (!hasDestination) {
      setRoutePoints([]);
      setRouteMeta(null);
      setRouteError("");
      return;
    }

    const controller = new AbortController();
    let isMounted = true;

    async function fetchRoute() {
      setRouteLoading(true);
      setRouteError("");

      const start = `${location.longitude},${location.latitude}`;
      const end = `${destination.longitude},${destination.latitude}`;
      const url = `${OSRM_ENDPOINT}/route/v1/driving/${start};${end}?overview=full&geometries=geojson`;

      try {
        const response = await fetch(url, { signal: controller.signal });
        if (!response.ok) {
          throw new Error("Unable to highlight navigation route");
        }

        const data = await response.json();
        if (data?.code === "Ok" && data?.routes?.length) {
          const route = data.routes[0];
          const coordinates = route.geometry?.coordinates?.map(([lng, lat]) => [lat, lng]) || [];

          if (isMounted) {
            setRoutePoints(coordinates);
            setRouteMeta({ distance: route.distance, duration: route.duration });
          }
        } else if (isMounted) {
          setRoutePoints([]);
          setRouteMeta(null);
          setRouteError("Unable to compute driving route. Showing straight-line distance.");
        }
      } catch (error) {
        if (error.name === "AbortError") return;
        if (isMounted) {
          setRoutePoints([]);
          setRouteMeta(null);
          setRouteError("Unable to load route guidance. Showing straight-line distance.");
        }
      } finally {
        if (isMounted) {
          setRouteLoading(false);
        }
      }
    }

    fetchRoute();

    return () => {
      isMounted = false;
      controller.abort();
    };
  }, [destination?.latitude, destination?.longitude, hasDestination, location?.latitude, location?.longitude]);

  const polylineToRender = routePoints.length ? routePoints : fallbackRoute;

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
        {polylineToRender.length >= 2 && (
          <Polyline positions={polylineToRender} color="#2563eb" weight={4} opacity={0.8} />
        )}
        <Recenter position={position} />
      </MapContainer>
      <div className="px-4 py-3 text-xs text-slate-600 bg-white border-t border-slate-100 space-y-1">
        <div>
          <p className="font-semibold text-slate-700">Service address</p>
          <p>{userAddress || "Not provided"}</p>
        </div>
        {hasDestination && (
          <div className="text-[11px] text-slate-500 space-y-1">
            {routeLoading && <p>Calculating best route…</p>}
            {!routeLoading && routeMeta && (
              <p>
                {`≈ ${(routeMeta.distance / 1000).toFixed(2)} km • ETA ${Math.max(1, Math.round(routeMeta.duration / 60))} min`}
              </p>
            )}
            {!routeLoading && !routeMeta && !routeError && fallbackRoute.length >= 2 && (
              <p>Showing straight-line distance until we can load a route.</p>
            )}
            {routeError && <p className="text-amber-600">{routeError}</p>}
          </div>
        )}
      </div>
    </div>
  );
}

export default LiveLocationMap;