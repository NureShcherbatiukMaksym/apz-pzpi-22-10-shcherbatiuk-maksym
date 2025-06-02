import React, { useEffect, useState } from 'react';
import api from '../../utils/api';
import {useTranslation} from "react-i18next";

const AdminDevices = () => {
    const [devices, setDevices] = useState([]);
    const [newDeviceNumber, setNewDeviceNumber] = useState('');
    const [editDeviceId, setEditDeviceId] = useState(null);
    const [editDeviceNumber, setEditDeviceNumber] = useState('');
    const { t } = useTranslation();
    useEffect(() => {
        fetchDevices();
    }, []);

    const fetchDevices = async () => {
        try {
            const res = await api.get('/iot-devices', { withCredentials: true });
            setDevices(res.data);
        } catch (err) {
            console.error('Error fetching devices:', err);
        }
    };

    const handleAddDevice = async () => {
        try {
            await api.post('/iot-devices', { device_name: newDeviceNumber }, { withCredentials: true });
            setNewDeviceNumber('');
            fetchDevices();
        } catch (err) {
            console.error('Error adding device:', err);
        }
    };

    const handleUpdateDevice = async (id) => {
        try {
            await api.put(`/iot-devices/${id}`, { device_name: editDeviceNumber }, { withCredentials: true });
            setEditDeviceId(null);
            setEditDeviceNumber('');
            fetchDevices();
        } catch (err) {
            console.error('Error updating device:', err);
        }
    };

    const handleDeleteDevice = async (id) => {
        try {
            await api.delete(`/iot-devices/${id}`, { withCredentials: true });
            fetchDevices();
        } catch (err) {
            console.error('Error deleting device:', err);
        }
    };

    return (
        <div className="p-4 overflow-y-auto h-screen w-full">
            <h2 className="text-2xl font-bold mb-4">{t(`device_management`)}</h2>

            <div className="mb-6 flex items-center">
                <input
                    type="text"
                    placeholder={t(`new_device_number`)}
                    value={newDeviceNumber}
                    onChange={(e) => setNewDeviceNumber(e.target.value)}
                    className="border px-6 py-2 mr-4 flex-1 min-w-0"
                />
                <button
                    onClick={handleAddDevice}
                    className="bg-indigo-500 text-white px-4 py-2 rounded hover:bg-indigo-600"
                >
                    {t(`add_device`)}
                </button>
            </div>

            <table className="w-full table-auto border-collapse border border-gray-200">
                <thead>
                <tr className="bg-gray-100">
                    <th className="border px-4 py-2">ID</th>
                    <th className="border px-4 py-2">{t(`device_number`)}</th>
                    <th className="border px-4 py-2">{t(`actions`)}</th>
                </tr>
                </thead>
                <tbody>
                {devices.map((device) => (
                    <tr key={device.id}>
                        <td className="border px-4 py-2 text-center">{device.id}</td>
                        <td className="border px-4 py-2">
                            {editDeviceId === device.id ? (
                                <input
                                    type="text"
                                    value={editDeviceNumber}
                                    onChange={(e) => setEditDeviceNumber(e.target.value)}
                                    className="border px-2 py-1 w-full"
                                />
                            ) : (
                                device.device_name
                            )}
                        </td>

                        <td className="border px-4 py-2 w-60">

                            <div className="flex justify-center space-x-2">
                                {editDeviceId === device.id ? (
                                    <button
                                        onClick={() => handleUpdateDevice(device.id)}
                                        className="bg-green-500 text-white px-2 py-1 rounded"
                                    >
                                        {t(`save_changes`)}
                                    </button>
                                ) : (
                                    <button
                                        onClick={() => {
                                            setEditDeviceId(device.id);
                                            setEditDeviceNumber(device.device_name);
                                        }}
                                        className="bg-yellow-400 text-white px-2 py-1 rounded"
                                    >
                                        {t(`edit`)}
                                    </button>
                                )}
                                <button
                                    onClick={() => handleDeleteDevice(device.id)}
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

export default AdminDevices;
