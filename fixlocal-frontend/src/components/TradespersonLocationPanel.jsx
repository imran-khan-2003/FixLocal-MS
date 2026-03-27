import { useCallback, useEffect, useMemo, useState } from "react";
import { MapContainer, Marker, Polyline, TileLayer } from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import { bookingService } from "../api/bookingService";

const decodePolyline = (coordinates) => {
  if (!coordinates) return [];
  return coordinates.map((pair) => [pair[1], pair[0]]);
};

const markerIcon = new L.Icon({
  iconUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png",
  iconRetinaUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png",
  shadowUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png",
  iconSize: [25, 41],
  iconAnchor: [12, 41],
});

function TradespersonLocationPanel({ booking }) {
  const [latitude, setLatitude] = useState("");
  const [longitude, setLongitude] = useState("");
  const [status, setStatus] = useState("");
  const [sending, setSending] = useState(false);
  const [routeStatus, setRouteStatus] = useState("");
  const [routeData, setRouteData] = useState(null);

  const canSend = useMemo(() => Boolean(booking), [booking]);
  const tradespersonPosition =
    latitude && longitude ? [Number(latitude), Number(longitude)] : null;
  const destinationPosition =
    booking?.userLatitude && booking?.userLongitude
      ? [booking.userLatitude, booking.userLongitude]
      : null;
  const routePolyline = routeData?.geometry?.length
    ? routeData.geometry
    : tradespersonPosition && destinationPosition
      ? [tradespersonPosition, destinationPosition]
      : [];
  const mapCenter = tradespersonPosition || destinationPosition;

  useEffect(() => {
    setLatitude("");
    setLongitude("");
    setRouteData(null);
    setRouteStatus("");
  }, [booking?.id]);

  const populateWithBrowserLocation = useCallback(() => {
    if (!navigator.geolocation) {
      setStatus("Geolocation not supported in this browser.");
      return;
    }
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setLatitude(pos.coords.latitude.toFixed(5));
        setLongitude(pos.coords.longitude.toFixed(5));
        setStatus("Location captured from device GPS.");
      },
      () => {
        setStatus("Unable to access device location. Please enter manually.");
      }
    );
  }, []);

  const shareLocation = useCallback(async () => {
    if (!booking) return;
    if (!latitude || !longitude) {
      setStatus("Enter latitude and longitude before sending.");
      return;
    }

    setSending(true);
    setStatus("");
    try {
      await bookingService.updateLiveLocation(booking.id, {
        bookingId: booking.id,
        latitude: Number(latitude),
        longitude: Number(longitude),
      });
      setStatus("Live location shared successfully.");
      if (booking.userLatitude && booking.userLongitude) {
        try {
          const response = await fetch(
            `https://router.project-osrm.org/route/v1/driving/${Number(longitude)},${Number(
              latitude
            )};${booking.userLongitude},${booking.userLatitude}?overview=full&geometries=geojson`
          );
          const data = await response.json();
          if (data?.code === "Ok" && data?.routes?.length) {
            setRouteData({
              distance: data.routes[0].distance,
              duration: data.routes[0].duration,
              geometry: decodePolyline(data.routes[0].geometry?.coordinates),
            });
            setRouteStatus("Route loaded.");
          } else {
            setRouteStatus("Unable to fetch route suggestion.");
          }
        } catch (routeErr) {
          setRouteStatus("Failed to load route info.");
        }
      }
    } catch (err) {
      setStatus("Failed to share location. Please try again.");
    } finally {
      setSending(false);
    }
  }, [booking, latitude, longitude]);

  if (!booking) {
    return (
      <div className="bg-white border border-slate-100 rounded-2xl p-4 text-sm text-slate-600">
        No active EN_ROUTE booking. Start a trip to share your live location with the user.
      </div>
    );
  }

  return (
    <div className="bg-white border border-slate-100 rounded-2xl p-4 space-y-3 shadow-sm">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-xs uppercase text-slate-500">Live location</p>
          <p className="text-base font-semibold text-slate-900">
            Share your current position
          </p>
          <p className="text-xs text-slate-500">
            User will see updates instantly. Updates expire automatically after 15 minutes.
          </p>
        </div>
        <button
          type="button"
          onClick={populateWithBrowserLocation}
          className="text-xs px-3 py-2 rounded-full border border-slate-200 text-slate-700 hover:bg-slate-50"
        >
          Use my GPS
        </button>
      </div>
      <div className="grid grid-cols-2 gap-3 text-sm">
        <label className="space-y-1">
          <span className="text-slate-500 text-xs">Latitude</span>
          <input
            type="number"
            step="0.00001"
            value={latitude}
            onChange={(e) => setLatitude(e.target.value)}
            className="w-full border border-slate-200 rounded-lg px-3 py-2"
          />
        </label>
        <label className="space-y-1">
          <span className="text-slate-500 text-xs">Longitude</span>
          <input
            type="number"
            step="0.00001"
            value={longitude}
            onChange={(e) => setLongitude(e.target.value)}
            className="w-full border border-slate-200 rounded-lg px-3 py-2"
          />
        </label>
      </div>
      {status && <p className="text-xs text-slate-600">{status}</p>}
      {routeStatus && <p className="text-xs text-slate-500">{routeStatus}</p>}
      {routeData && (
        <div className="text-xs text-slate-500">
          <p>
            Distance: {(routeData.distance / 1000).toFixed(2)} km • ETA: {Math.round(routeData.duration / 60)} min
          </p>
          {booking?.userCity && <p>Destination city: {booking.userCity}</p>}
        </div>
      )}
      {mapCenter && destinationPosition && (
        <div className="border border-slate-100 rounded-xl overflow-hidden">
          <MapContainer
            center={mapCenter}
            zoom={13}
            style={{ height: "250px", width: "100%" }}
            scrollWheelZoom={false}
          >
            <TileLayer
              attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
              url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            />
            {tradespersonPosition && (
              <Marker position={tradespersonPosition} icon={markerIcon} />
            )}
            <Marker position={destinationPosition} icon={markerIcon} />
            {routePolyline.length >= 2 && (
              <Polyline positions={routePolyline} color="#2563eb" weight={4} opacity={0.8} />
            )}
          </MapContainer>
        </div>
      )}
      <button
        type="button"
        disabled={!canSend || sending}
        onClick={shareLocation}
        className="w-full bg-blue-600 text-white text-sm font-semibold px-4 py-2 rounded-lg disabled:opacity-60"
      >
        {sending ? "Sending…" : "Share location"}
      </button>
    </div>
  );
}

export default TradespersonLocationPanel;