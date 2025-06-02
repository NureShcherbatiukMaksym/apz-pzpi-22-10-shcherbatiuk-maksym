import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from "react-router-dom";
import api from "../utils/api";
import MapPolygonPicker from './MapPolygonPicker';
import {useTranslation} from "react-i18next"; // імпорт компонента мапи

const CreateField = () => {
    const navigate = useNavigate();
    const { id } = useParams(); // отримуємо id з URL
    const { t } = useTranslation();
    const [name, setName] = useState('');
    const [area, setArea] = useState('');
    const [geoZone, setGeoZone] = useState(null);
    const [manualCoords, setManualCoords] = useState('');

    useEffect(() => {
        if (id) {
            api.get(`/fields/${id}`).then(res => {
                const field = res.data;
                setName(field.name);
                setArea(field.area);
                if (field.geo_zone?.coordinates?.[0]) {
                    let coords = field.geo_zone.coordinates[0];


                    if (coords.length > 1 &&
                        coords[0][0] === coords[coords.length - 1][0] &&
                        coords[0][1] === coords[coords.length - 1][1]
                    ) {
                        coords = coords.slice(0, -1);
                    }

                    setGeoZone(coords);
                    setManualCoords(coords
                        .map(pair => `[${pair[0]}, ${pair[1]}]`)
                        .join(',\n'));
                }



            }).catch(err => {
                console.error("Помилка при завантаженні поля", err);
                alert("Не вдалося завантажити поле");
            });
        }
        else {
            setName('');
            setArea('');
            setGeoZone(null);
            setManualCoords('');
        }
    }, [id]);

    useEffect(() => {
        if (geoZone && geoZone.length >= 3) {
            const hectares = calculatePolygonArea(geoZone);
            setArea(hectares.toFixed(3));
        }
    }, [geoZone]);

    const handleCancel = () => {
        navigate('/home');

    };

    const parseManualCoords = () => {
        try {
            const cleaned = manualCoords
                .replace(/\s+/g, '')
                .replace(/\],\[/g, ']~[')
                .split('~')
                .map(pair => JSON.parse(pair));

            if (!Array.isArray(cleaned) || cleaned.length < 3) throw new Error("Недостатньо координат");
            return cleaned;
        } catch (e) {
            alert(`${t(`coordinates_exceptions`)}`);
            return null;
        }
    };


    const handleSubmit = async (e) => {
        e.preventDefault();

        let coordinates = parseManualCoords();


        if (!coordinates) {
            alert(`${t('map_pick_exceptions')}`);
            return;
        }

        const firstPoint = coordinates[0];
        const lastPoint = coordinates[coordinates.length - 1];

        const isClosed = firstPoint[0] === lastPoint[0] && firstPoint[1] === lastPoint[1];

        if (!isClosed) {
            coordinates.push(firstPoint);
        }

        const payload = {
            name,
            area: parseFloat(area),
            geo_zone: {
                type: "Polygon",
                coordinates: [coordinates],
            },
        };

        try {
            if (id) {
                await api.put(`/fields/${id}`, payload);
                alert("Поле оновлено!");
            } else {
                await api.post('/fields', payload);
                alert("Поле успішно створене!");
            }
            navigate("/home");
        } catch (error) {
            console.error("Помилка:", error);
            alert(error?.response?.data?.error || "Щось пішло не так");
        }
    };


    const calculatePolygonArea = (coords) => {
        if (!window.google || !coords || coords.length < 3) return 0;

        const path = coords.map(([lat, lng]) => new window.google.maps.LatLng(lat, lng));
        const areaMetersSquared = window.google.maps.geometry.spherical.computeArea(path);
        const SQUARE_METERS_IN_HECTARE = 10000;
        const areaHectares = areaMetersSquared / SQUARE_METERS_IN_HECTARE;
        return areaHectares;
    };
    const [showConfirm, setShowConfirm] = useState(false);
    const handleDelete = async () => {
        if (!id) return;

        try {
            await api.delete(`/fields/${id}`);
            alert("Поле успішно видалено!");
            navigate('/home');
        } catch (error) {
            console.error("Помилка при видаленні:", error);
            alert(error?.response?.data?.message || "Не вдалося видалити поле");
        }
    };

    const handleConfirmDelete = () => {
        setShowConfirm(true);
    };

    const handleCancelDelete = () => {
        setShowConfirm(false);
    };

    const handleConfirm = async () => {
        setShowConfirm(false);
        await handleDelete();
    };

    return (
        <div className="w-full flex-1">
            <div className="p-6">
                <h5 className="font-bold text-black text-xl mb-4">{id ? t('edit_field') : t('add_field_title')}</h5>
                <form onSubmit={handleSubmit}>
                    <label className="block mb-4">
                        {t('field_name')}
                        <input
                            type="text"
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                            className="w-full border border-gray-300 p-2 rounded-lg"
                            required
                        />
                    </label>
                    <label className="block mb-4">
                        {t('area_description')}
                        <input
                            type="number"
                            value={area}
                            onChange={(e) => setArea(e.target.value)}
                            className="w-full border border-gray-300 p-2 rounded-lg"
                            required
                            step="0.01"
                        />
                    </label>

                    {/* Карта */}
                    <div className="my-6">
                        <h6 className="font-semibold mb-2">{t('map_pick')}</h6>
                        <div className="h-80 border rounded-lg flex items-center justify-center text-gray-500">
                            <MapPolygonPicker
                                onPolygonComplete={(coords) => {
                                    setGeoZone(coords);
                                    const hectares = calculatePolygonArea(coords);
                                    setArea(hectares.toFixed(3));
                                }}
                                setManualCoordsFromMap={(coordsText) => setManualCoords(coordsText)}
                                initialPolygon={geoZone}
                                resetSignal={id}
                            />



                        </div>
                    </div>


                    <div className="my-6">
                        <h6 className="font-semibold mb-2">{t('coordinates')}</h6>
                        <textarea
                            rows="6"
                            value={manualCoords}
                            onChange={(e) => setManualCoords(e.target.value)}
                            className="w-full border border-gray-300 p-2 rounded-lg font-mono text-sm"
                            placeholder={`[40.7484, -73.9857],\n[40.7488, -73.9855],\n[40.7486, -73.9842],\n[40.7482, -73.9844],\n[40.7484, -73.9857]`}
                        />
                        <p className="text-xs text-gray-500 mt-2">
                            {t('coordinates_instruction')}
                        </p>
                    </div>

                    <div className="flex flex-wrap items-center gap-4 mt-6">
                        <button
                            type="submit"
                            className="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-full"
                        >
                            {id ? t('edit_field') : t('add_field_title')}
                        </button>

                        <button
                            type="button"
                            onClick={handleCancel}
                            className="bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded-full font-semibold"
                        >
                            {t('cancel')}
                        </button>

                        {id && (
                            <button
                                type="button"
                                onClick={handleConfirmDelete}
                                className="bg-red-500 hover:bg-red-800 text-white px-4 py-2 rounded-full font-semibold ml-auto"
                            >
                                {t('delete_field')}
                            </button>
                        )}


                        {showConfirm && (
                            <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50">
                                <div className="bg-white p-6 rounded-2xl shadow-xl w-100 text-center">
                                    <h2 className="text-xl font-semibold mb-4">{t('delete_field_question')}</h2>
                                    <p className="mb-6 text-gray-600">{t('delete_field_description')}</p>
                                    <div className="flex justify-center gap-4">
                                        <button
                                            onClick={handleConfirm}
                                            className="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded-full font-semibold"
                                        >
                                            {t('delete_field_confirm')}
                                        </button>
                                        <button
                                            onClick={handleCancelDelete}
                                            className="bg-gray-300 hover:bg-gray-400 text-black px-4 py-2 rounded-full font-semibold"
                                        >
                                            {t('cancel')}
                                        </button>
                                    </div>
                                </div>
                            </div>
                        )}
                    </div>

                </form>
            </div>
        </div>
    );
};

export default CreateField;
