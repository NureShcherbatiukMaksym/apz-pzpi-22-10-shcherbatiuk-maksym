import React, { useEffect, useState } from 'react';
import api from '../../utils/api';
import {useTranslation} from "react-i18next";

const AdminUserDevices = () => {
    const [users, setUsers] = useState([]);
    const [devices, setDevices] = useState([]);
    const [associations, setAssociations] = useState([]);
    const [newAssociation, setNewAssociation] = useState({ user_id: '', iot_device_id: '' });
    const { t } = useTranslation();


    const fetchUsers = async () => {
        try {
            const res = await api.get('/users');
            setUsers(res.data);
        } catch (err) {
            console.error('Error fetching users:', err);
        }
    };


    const fetchDevices = async () => {
        try {

            const res = await api.get('/iot-devices', { withCredentials: true });
            setDevices(res.data);
        } catch (err) {
            console.error('Error fetching devices:', err);
        }
    };


    const fetchAssociations = async () => {
        try {

            const res = await api.get('/user-iot-devices/all');

            setAssociations(res.data);
            console.log('Fetched associations:', res.data);
        } catch (err) {
            console.error('Error fetching associations:', err);
        }
    };

    useEffect(() => {

        fetchUsers();
        fetchDevices();
        fetchAssociations();
    }, []);


    const handleBind = async () => {

        if (!newAssociation.user_id || !newAssociation.iot_device_id) {
            alert("Будь ласка, оберіть користувача та пристрій.");
            return;
        }
        try {

            await api.post('/user-iot-devices', newAssociation);
            setNewAssociation({ user_id: '', iot_device_id: '' });
            fetchAssociations();
        } catch (error) {
            console.error('Помилка при прив’язці пристрою:', error);
            alert('Помилка при прив’язці пристрою: ' + (error.response?.data?.message || error.message));
        }
    };


    const handleUnbind = async (association) => {

        try {
            await api.delete('/user-iot-devices', {
                data: {
                    user_id: association.user_id,
                    iot_device_id: association.iot_device_id
                }
            });
            fetchAssociations();
        } catch (error) {
            console.error('Помилка при відв’язці пристрою:', error);
            alert('Помилка при відв’язці пристрою: ' + (error.response?.data?.message || error.message));
        }
        /*
        // Alternative DELETE by association ID (if your backend supports DELETE /user-iot-devices/:id)
        try {
            await api.delete(`/user-iot-devices/${association.id}`); // Assumes assoc has id
            fetchAssociations(); // Refresh the list
        } catch (error) {
            console.error('Помилка при відв’язці пристрою:', error);
            alert('Помилка при відв’язці пристрою: ' + (error.response?.data?.message || error.message));
        }
        */
    };

    return (
        <div className="p-4 overflow-y-auto h-screen w-full">
            <h2 className="text-2xl font-bold mb-4">{t(`users_devices_management`)}</h2>

            <div className="mb-4 grid grid-cols-3 gap-2">
                <select
                    value={newAssociation.user_id}
                    onChange={(e) => setNewAssociation({ ...newAssociation, user_id: e.target.value })}
                    className="border p-2"
                >
                    <option value="">{t(`select_user`)}</option>
                    {users.map(user => (
                        <option key={user.id} value={user.id}>{user.email}</option>
                    ))}
                </select>

                <select
                    value={newAssociation.iot_device_id}
                    onChange={(e) => setNewAssociation({ ...newAssociation, iot_device_id: e.target.value })}
                    className="border p-2"
                >
                    <option value="">{t(`select_device`)}</option>
                    {devices.map(device => (
                        <option key={device.id} value={device.id}>{device.device_name || device.id}</option>
                    ))}
                </select>

                <button onClick={handleBind} className="bg-blue-500 text-white p-2 rounded">
                    {t(`bind`)}
                </button>
            </div>


            <h3 className="text-lg font-semibold mb-2">{t(`existing_bindings`)}</h3>
            {associations.length === 0 && (
                <p>{t(`existing_bindings_exceptions`)}</p>
            )}
            {associations.length > 0 && (
                <table className="w-full border border-collapse">
                    <thead>
                    <tr>
                        <th className="border p-2 text-center">ID</th>
                        <th className="border p-2 text-center">{t(`user`)}</th>
                        <th className="border p-2 text-center">{t(`device`)}</th>
                        <th className="border p-2 text-center w-1/6">{t(`actions`)}</th>
                    </tr>
                    </thead>
                    <tbody>

                    {associations.map((assoc) => (
                        <tr key={assoc.id || `${assoc.user_id}-${assoc.iot_device_id}`}>
                            <td className="border p-2 text-center">{assoc.id}</td>
                            <td className="border p-2">{assoc.User?.email || '—'}</td>
                            <td className="border p-2">{assoc.IotDevice?.device_name || '—'}</td>
                            <td className="border p-2 w-1/6">
                                <div className="flex justify-center space-x-2">
                                    <button
                                        onClick={() => handleUnbind(assoc)}
                                        className="bg-red-500 text-white p-1 px-2 rounded"
                                    >
                                        {t(`unbind`)}
                                    </button>
                                </div>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            )}
        </div>
    );
};
export default AdminUserDevices;