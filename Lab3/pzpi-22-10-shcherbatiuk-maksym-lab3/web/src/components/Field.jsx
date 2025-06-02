import React, { useEffect, useState } from 'react';
import { useParams } from "react-router-dom";
import api from '../utils/api';
import { io } from 'socket.io-client';
import {useTranslation} from "react-i18next";

const Field = () => {
    const { id } = useParams();
    const [field, setField] = useState(null);
    const [points, setPoints] = useState([]);
    const [socket, setSocket] = useState(null);
    const { t } = useTranslation();
    // Вибір поля
    const selectField = async () => {
        try {
            await api.post('/fields/select/', { fieldId: id });
        } catch (err) {
            console.error('Не вдалося вибрати поле:', err);
        }
    };

    const deselectField = async () => {
        try {
            await api.post('/fields/deselect/', { fieldId: id });
        } catch (err) {
            console.error('Не вдалося деселектнути поле:', err);
        }
    };

    const fetchPoints = async () => {
        try {
            const response = await api.get('/measurement-points/points');
            setPoints(response.data);
        } catch (error) {
            console.error("Error fetching points:", error);
        }
    };

    const activatePoint = async (pointId) => {
        try {

            await api.post('/measurement-points/activate', { pointId: pointId, fieldId: id }); // Додали fieldId
        } catch (err) {
            console.error('Помилка при активації точки:', err);
        }
    };

    const deactivatePoint = async (pointId) => {
        try {
            await api.post('/measurement-points/deactivate', {pointId: pointId, fieldId: id });
        } catch (err) {
            console.error('Помилка при деактивації точки:', err);
        }
    };

    useEffect(() => {
        const init = async () => {
            try {
                setPoints([]);

                const fieldResponse = await api.get(`/fields/${id}`);
                setField(fieldResponse.data);

                await selectField();
                await fetchPoints();
            } catch (err) {
                console.error("Помилка ініціалізації поля:", err);
            }
        };

        init();

        return () => {
            deselectField();
        };
    }, [id]);

    useEffect(() => {
        const socket = io('ws://localhost:5000', {
            transports: ['websocket'],
            reconnection: true,
            reconnectionAttempts: 5,
            timeout: 10000
        });

        setSocket(socket);

        socket.on('connect', () => {
            console.log('🟢 Socket.io connected');
            socket.emit('subscribe', { fieldId: id });

        });

        socket.on('connect_error', (err) => {
            console.error('Помилка WebSocket з’єднання:', err.message);
        });

        socket.on('connect_error', (err) => {
            console.error('❌ Socket connection error:', err.message);
        });


        socket.on('update', async (data) => {
            console.log('📥 Update from server:', data);
            if (data.type === 'pointActivated') {
                setPoints((prev) =>
                    prev.map(p => p.id === data.pointId ? { ...p, active: true } : p)
                );
            } else if (data.type === 'pointDeactivated') {
                setPoints((prev) =>
                    prev.map(p => p.id === data.pointId ? { ...p, active: false } : p)
                );
            }
            await fetchPoints();
        });

        socket.on('disconnect', () => {
            console.log('🔴 Socket.io disconnected');
        });

        return () => {
            socket.disconnect();
        };
    }, [id]);



    if (!field) return <div className="p-4">{t(`loading`)}</div>;

    return (
        <div className="p-6 w-full flex-1">
            <h1 className="text-3xl font-bold mb-2">{field.name}</h1>
            <p className="text-gray-600 mb-4">{field.description}</p>

            <div className="grid grid-cols-2 gap-4">
                <p><strong>{t(`area`)}</strong> {field.area} {t(`hectares`)}</p>
                <p><strong>{t(`created_at`)}</strong> {new Date(field.created_at).toLocaleDateString()}</p>
            </div>

            <hr className="my-6" />

            <h2 className="text-xl font-semibold mb-4 mt-6">{t(`field_points`)}</h2>

            <div className="max-h-96 overflow-y-auto pr-2 border rounded-lg shadow-sm bg-white">
                {points.length === 0 ? (
                    <p className="p-4 text-gray-500 italic">{t(`points_loading`)}</p>
                ) : (
                    <ul className="divide-y divide-gray-200">
                        {points.map((point) => (
                            <PointItem
                                key={point.id}
                                point={point}
                                onActivate={activatePoint}
                                onDeactivate={deactivatePoint}
                            />
                        ))}
                    </ul>
                )}
            </div>
        </div>
    );
};


const PointItem = React.memo(({ point, onActivate, onDeactivate }) => {
    const { t } = useTranslation();
    return (
        <li key={point.id} className="flex items-center px-4 py-3 hover:bg-gray-50">
            <div className="h-6 px-3 flex-shrink-0 text-center text-l font-semibold text-gray-700 bg-gray-100 shadow-md rounded-md whitespace-nowrap">
                №{point.point_order}
            </div>

            <div className="w-px h-16 bg-gray-300 mx-4"></div>

            <div className="flex-1">
                <div className="flex justify-between items-center ">
                    <div className="text-l text-gray-800">
                        lat: {point.latitude.toFixed(6)}
                    </div>
                    <div className="text-sm text-gray-800">
                        Статус:{" "}
                        <span className={point.active ? "text-green-600" : "text-red-500"}>
                            {point.active ? t(`status_active`) : t(`status_inactive`)}
                        </span>
                    </div>
                    <button
                        onClick={() =>
                            point.active ? onDeactivate(point.id) : onActivate(point.id)
                        }
                        className={`text-sm px-3 py-1 rounded ${
                            point.active
                                ? "bg-red-100 text-red-600 hover:bg-red-200"
                                : "bg-green-100 text-green-600 hover:bg-green-200"
                        }`}
                    >
                        {point.active ? t(`deactivate_point`) : t(`activate_point`)}
                    </button>
                </div>

                <div className="mt-1 text-sm text-gray-600">
                    lng: {point.longitude.toFixed(6)}
                </div>
            </div>
        </li>
    );
});

export default Field;
