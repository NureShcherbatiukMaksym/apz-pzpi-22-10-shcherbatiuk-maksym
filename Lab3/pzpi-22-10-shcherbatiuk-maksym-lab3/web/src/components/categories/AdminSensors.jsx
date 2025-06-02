import React, { useEffect, useState } from 'react';
import api from '../../utils/api';
import {useTranslation} from "react-i18next";

const AdminSensors = () => {
    const [sensors, setSensors] = useState([]);
    const [newSensorType, setNewSensorType] = useState('');
    const [newSensorRadius, setNewSensorRadius] = useState('');
    const [newSensorStatus, setNewSensorStatus] = useState('active');
    const [newSensorUnit, setNewSensorUnit] = useState('');
    const [editSensorId, setEditSensorId] = useState(null);
    const [editSensorType, setEditSensorType] = useState('');
    const [editSensorRadius, setEditSensorRadius] = useState('');
    const [editSensorStatus, setEditSensorStatus] = useState('');
    const [editSensorUnit, setEditSensorUnit] = useState('');
    const { t } = useTranslation();
    const statusOptions = ['active', 'inactive', 'maintenance', 'calibrating', 'error', 'retired'];
    const typeOptions = ['temperature', 'soil_moisture', 'acidity'];

    useEffect(() => {
        fetchSensors();
    }, []);

    const fetchSensors = async () => {
        try {
            const res = await api.get('/sensors', { withCredentials: true });
            setSensors(res.data);
        } catch (err) {
            console.error('Error fetching sensors:', err);
        }
    };

    const handleAddSensor = async () => {
        try {
            await api.post('/sensors',
                {
                    type: newSensorType,
                    radius: newSensorRadius,
                    status: newSensorStatus,
                    unit: newSensorUnit
                },
                { withCredentials: true }
            );
            setNewSensorType('');
            setNewSensorRadius('');
            setNewSensorStatus('active');
            setNewSensorUnit('');
            fetchSensors();
        } catch (err) {
            console.error('Error adding sensor:', err);
        }
    };

    const handleUpdateSensor = async (id) => {
        try {
            await api.put(`/sensors/${id}`,
                {
                    type: editSensorType,
                    radius: editSensorRadius,
                    status: editSensorStatus,
                    unit: editSensorUnit
                },
                { withCredentials: true }
            );
            setEditSensorId(null);
            setEditSensorType('');
            setEditSensorRadius('');
            setEditSensorStatus('');
            setEditSensorUnit('');
            fetchSensors();
        } catch (err) {
            console.error('Error updating sensor:', err);
        }
    };

    const handleDeleteSensor = async (id) => {
        try {
            await api.delete(`/sensors/${id}`, { withCredentials: true });
            fetchSensors();
        } catch (err) {
            console.error('Error deleting sensor:', err);
        }
    };

    return (
        <div className=" p-4 overflow-y-auto h-screen w-full">
            <h2 className="text-2xl font-bold mb-4">{t(`sensor_management`)}</h2>

            <div className="mb-6">
                <select
                    value={newSensorType}
                    onChange={(e) => setNewSensorType(e.target.value)}
                    className="border px-4 py-2 mr-2"
                >
                    <option value="">{t(`select_sensor_type`)}</option>
                    {typeOptions.map((type) => (
                        <option key={type} value={type}>{type}</option>
                    ))}
                </select>
                <input
                    type="number"
                    placeholder={t(`sensor_radius`)}
                    value={newSensorRadius}
                    onChange={(e) => setNewSensorRadius(e.target.value)}
                    className="border px-4 py-2 mr-2"
                />
                <select
                    value={newSensorStatus}
                    onChange={(e) => setNewSensorStatus(e.target.value)}
                    className="border px-4 py-2 mr-2"
                >
                    {statusOptions.map((status) => (
                        <option key={status} value={status}>{status}</option>
                    ))}
                </select>
                <select
                    value={newSensorUnit}
                    onChange={(e) => setNewSensorUnit(e.target.value)}
                    className="border px-4 py-2 mr-2"
                >
                    <option value="">{t(`sensor_unit`)}</option>
                    <option value="°C">°C</option>
                    <option value="%">%</option>
                    <option value="pH">pH</option>
                </select>
                <button
                    onClick={handleAddSensor}
                    className="bg-indigo-500 text-white px-4 py-2 rounded hover:bg-indigo-600"
                >
                    {t(`add_sensor`)}
                </button>
            </div>

            <table className="w-full table-auto border-collapse border border-gray-200">
                <thead>
                <tr className="bg-gray-100">
                    <th className="border px-4 py-2 ">ID</th>
                    <th className="border px-4 py-2">{t(`type`)}</th>
                    <th className="border px-4 py-2">{t(`radius`)}</th>
                    <th className="border px-4 py-2">{t(`sensor_status`)}</th>
                    <th className="border px-4 py-2">{t(`unit`)}</th>
                    <th className="border px-4 py-2">{t(`actions`)}</th>
                </tr>
                </thead>
                <tbody>
                {sensors.map((sensor) => (
                    <tr key={sensor.id}>
                        <td className="border px-4 py-2 text-center">{sensor.id}</td>
                        <td className="border px-4 py-2">
                            {editSensorId === sensor.id ? (
                                <select
                                    value={editSensorType}
                                    onChange={(e) => setEditSensorType(e.target.value)}
                                    className="border px-2 py-1"
                                >
                                    {typeOptions.map((type) => (
                                        <option key={type} value={type}>{type}</option>
                                    ))}
                                </select>
                            ) : (
                                sensor.type
                            )}
                        </td>
                        <td className="border px-4 py-2">
                            {editSensorId === sensor.id ? (
                                <input
                                    type="number"
                                    value={editSensorRadius}
                                    onChange={(e) => setEditSensorRadius(e.target.value)}
                                    className="border px-2 py-1"
                                />
                            ) : (
                                sensor.radius
                            )}
                        </td>
                        <td className="border px-4 py-2">
                            {editSensorId === sensor.id ? (
                                <select
                                    value={editSensorStatus}
                                    onChange={(e) => setEditSensorStatus(e.target.value)}
                                    className="border px-2 py-1"
                                >
                                    {statusOptions.map((status) => (
                                        <option key={status} value={status}>{status}</option>
                                    ))}
                                </select>
                            ) : (
                                sensor.status
                            )}
                        </td>
                        <td className="border px-4 py-2">
                            {editSensorId === sensor.id ? (
                                <select
                                    value={editSensorUnit}
                                    onChange={(e) => setEditSensorUnit(e.target.value)}
                                    className="border px-2 py-1"
                                >
                                    <option value="°C">°C</option>
                                    <option value="%">%</option>
                                    <option value="pH">pH</option>
                                </select>
                            ) : (
                                sensor.unit
                            )}
                        </td>
                        <td className="border px-4 py-2 w-60">
                            <div className="flex justify-center space-x-2">
                                {editSensorId === sensor.id ? (
                                    <button
                                        onClick={() => handleUpdateSensor(sensor.id)}
                                        className="bg-green-500 text-white px-2 py-1 rounded"
                                    >
                                        {t(`save_changes`)}
                                    </button>
                                ) : (
                                    <button
                                        onClick={() => {
                                            setEditSensorId(sensor.id);
                                            setEditSensorType(sensor.type);
                                            setEditSensorRadius(sensor.radius);
                                            setEditSensorStatus(sensor.status);
                                            setEditSensorUnit(sensor.unit);
                                        }}
                                        className="bg-yellow-400 text-white px-2 py-1 rounded"
                                    >
                                        {t(`edit`)}
                                    </button>
                                )}
                                <button
                                    onClick={() => handleDeleteSensor(sensor.id)}
                                    className="bg-red-500 text-white px-2 py-1 rounded"
                                >
                                    {t(`delete`)}
                                </button>
                            </div>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
};

export default AdminSensors;
