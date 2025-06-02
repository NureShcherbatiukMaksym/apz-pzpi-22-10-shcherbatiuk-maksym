import React, { useCallback, useState, useRef, useEffect } from "react";
import { GoogleMap, Polygon } from "@react-google-maps/api";
import {useTranslation} from "react-i18next";

const containerStyle = {
    width: "100%",
    height: "100%",
};

const center = {
    lat: 48.3794,
    lng: 31.1656,
};

const MapPolygonPicker = ({ onPolygonComplete, setManualCoordsFromMap, initialPolygon }) => {
    const [paths, setPaths] = useState([]);
    const mapRef = useRef(null);
    const polygonRef = useRef(null);
    const { t } = useTranslation();

    useEffect(() => {
        if (initialPolygon && initialPolygon.length > 2) {
            let trimmed = initialPolygon;

            if (
                initialPolygon[0][0] === initialPolygon[initialPolygon.length - 1][0] &&
                initialPolygon[0][1] === initialPolygon[initialPolygon.length - 1][1]
            ) {
                trimmed = initialPolygon.slice(0, -1);
            }

            const converted = trimmed.map(([lat, lng]) => ({ lat, lng }));
            setPaths(converted);

            if (mapRef.current && window.google) {
                const bounds = new window.google.maps.LatLngBounds();
                converted.forEach(point => bounds.extend(point));
                mapRef.current.fitBounds(bounds);
            }
        }
    }, [initialPolygon]);




    const handleClick = useCallback((event) => {
        const newPoint = {
            lat: parseFloat(event.latLng.lat().toFixed(6)),
            lng: parseFloat(event.latLng.lng().toFixed(6)),
        };
        setPaths(prev => {
            const newPaths = [...prev, newPoint];
            onPolygonComplete(newPaths.map(p => [p.lat, p.lng]));
            return newPaths;
        });


    }, []);


    const updateManualCoords = (newPaths) => {
        if (setManualCoordsFromMap && newPaths.length > 0) {
            const coordsText = newPaths
                .map((point) => `[${point.lat}, ${point.lng}]`)
                .join(",\n");
            setManualCoordsFromMap(coordsText);
        }
    };

    useEffect(() => {
        updateManualCoords(paths);
    }, [paths]);


    const handleReset = () => {
        setPaths([]);
        if (setManualCoordsFromMap) {
            setManualCoordsFromMap("");
        }
        polygonRef.current = null;
    };


    const onEdit = () => {
        if (!polygonRef.current) return;
        const path = polygonRef.current.getPath();
        const updated = [];
        for (let i = 0; i < path.getLength(); i++) {
            const point = path.getAt(i);
            updated.push({
                lat: parseFloat(point.lat().toFixed(6)),
                lng: parseFloat(point.lng().toFixed(6)),
            });
        }
        setPaths(updated);
        updateManualCoords(updated);
        onPolygonComplete(updated.map(p => [p.lat, p.lng]));

    };

    return (
        <div className="w-full h-full relative">
            <GoogleMap
                mapContainerStyle={containerStyle}
                center={center}
                zoom={6}
                onClick={handleClick}
                onLoad={(map) => (mapRef.current = map)}
            >
                {paths.length > 0 && (
                    <Polygon
                        path={paths}
                        options={{
                            fillColor: "#00FF00",
                            fillOpacity: 0.3,
                            strokeColor: "#00AA00",
                            strokeOpacity: 1,
                            strokeWeight: 2,
                            editable: true,
                            draggable: false,
                        }}
                        onMouseUp={onEdit}
                        onDragEnd={onEdit}
                        onLoad={(polygon) => (polygonRef.current = polygon)}
                    />
                )}
            </GoogleMap>

            <div className="absolute bottom-2 right-2 flex gap-2">

                <button
                    type="button"
                    className="bg-red-500 text-white px-3 py-1 rounded text-sm shadow-md"
                    onClick={handleReset}
                >
                    {t('reset')}
                </button>
            </div>
        </div>
    );
};

export default MapPolygonPicker;
