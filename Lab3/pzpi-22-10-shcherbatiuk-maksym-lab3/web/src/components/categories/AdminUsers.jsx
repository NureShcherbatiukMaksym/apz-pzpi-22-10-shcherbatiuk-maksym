import React, { useEffect, useState, useMemo } from 'react';
import api from '../../utils/api';
import {useTranslation} from "react-i18next";

const AdminUsers = () => {
    const [users, setUsers] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [selectedUser, setSelectedUser] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const { t } = useTranslation();

    useEffect(() => {
        fetchUsers();
    }, []);

    const fetchUsers = async () => {
        try {
            setLoading(true);
            setError('');
            const res = await api.get('/users');
            setUsers(res.data);
        } catch (err) {
            console.error('Failed to fetch users:', err);
            setError(t('failed_load_users'));
        } finally {
            setLoading(false);
        }
    };

    const handleUserClick = async (user) => {

        if (selectedUser?.id === user.id) {
            console.log(`User ${user.id} clicked again, deselecting.`);
            setSelectedUser(null);
            return;
        }


        console.log(`Workspaceing details for user ${user.id}...`);
        try {
            setError('');
            const res = await api.get(`/users/${user.id}`);
            setSelectedUser(res.data);
            console.log(`Details fetched for user ${user.id}.`);
        } catch (err) {
            console.error(`Failed to fetch details for user ${user.id}:`, err);
            setError(t('failed_load_user_details'));
            setSelectedUser(null);
        }
    };


    const handleDelete = async (id) => {
        if (!window.confirm(t('confirm_delete_user'))) return;

        try {
            setError('');
            await api.delete(`/users/${id}`);
            setUsers(users.filter(user => user.id !== id));
            if (selectedUser?.id === id) {
                setSelectedUser(null);
            }
            console.log(`User ${id} deleted successfully.`);
        } catch (err) {
            console.error(`Failed to delete user ${id}:`, err);
            setError(t('failed_delete_user'));
        }
    };

    const filteredUsers = useMemo(() => {
        if (!searchTerm) {
            return users;
        }
        const lowerCaseSearchTerm = searchTerm.toLowerCase();
        return users.filter(user =>
            user.name.toLowerCase().includes(lowerCaseSearchTerm) ||
            String(user.id).toLowerCase().includes(lowerCaseSearchTerm)
        );
    }, [users, searchTerm]);



    return (
        <div className="p-4 overflow-y-auto h-screen w-full">
            <h2 className="text-2xl font-bold mb-4">{t(`users`)}</h2>

            <div className="mb-4">
                <input
                    type="text"
                    placeholder={t('search_users')}
                    value={searchTerm}
                    onChange={(e) => {
                        setSearchTerm(e.target.value);
                        setSelectedUser(null);
                    }}
                    className="border p-2 rounded w-full"
                />
            </div>

            {loading && <p>{t(`loading`)}</p>}
            {error && <p className="text-red-500">{error}</p>}

            {!loading && !error && filteredUsers.length === 0 && (
                <p className="text-gray-500">
                    {searchTerm
                        ? t('no_users_matching_search')
                        : t('no_users_found')
                    }
                </p>
            )}

            {filteredUsers.map(user => (
                <React.Fragment key={user.id}>

                    <div
                        onClick={() => handleUserClick(user)}
                        className={`border p-3 rounded mb-2 flex justify-between items-center cursor-pointer transition-colors duration-150
                            ${selectedUser?.id === user.id
                            ? 'bg-blue-100 border-blue-500'
                            : 'border-gray-300 hover:bg-gray-100'
                        }`}
                    >

                        <div className="h-6 px-3 flex-shrink-0 text-center font-semibold text-gray-700 bg-gray-100 shadow-md rounded-md whitespace-nowrap flex items-center justify-center">
                            {user.id}
                        </div>


                        <div className="w-px h-12 bg-gray-300 mx-4"></div>

                        <div className="flex-1 min-w-0 px-4">
                            <p><strong>{user.name}</strong></p>
                            <p className="text-sm text-gray-500">{user.email}</p>
                        </div>


                        <div className="flex gap-2 flex-shrink-0">
                            <button
                                onClick={(e) => {
                                    e.stopPropagation();
                                    handleDelete(user.id);
                                }}
                                className="text-red-500 hover:underline"
                            >
                                {t(`delete`)}
                            </button>
                        </div>
                    </div>

                    {selectedUser?.id === user.id && (
                        <div className="bg-white p-4 rounded shadow mb-4 ml-4 mr-4">
                            <h3 className="text-lg font-semibold mb-2">{t(`user_information`)}</h3>
                            <div className="space-y-2">
                                <p><strong>ID:</strong> {selectedUser.id}</p>
                                <p><strong>{t(`first_name`)}:</strong> {selectedUser.name}</p>
                                <p><strong>Email:</strong> {selectedUser.email}</p>
                                <p><strong>{t(`admin`)}:</strong> {selectedUser.is_admin ? t(`yes`) : t(`no`)}</p>
                                {selectedUser.profile_picture_url && (
                                    <p><strong>{t(`profile_picture`)}:</strong> <img src={selectedUser.profile_picture_url} alt="Profile" className="w-10 h-10 rounded-full inline-block ml-2" /></p>
                                )}
                            </div>
                        </div>
                    )}
                </React.Fragment>
            ))}
        </div>
    );
};

export default AdminUsers;