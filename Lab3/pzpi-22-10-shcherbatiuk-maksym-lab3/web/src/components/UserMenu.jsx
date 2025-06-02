import React, { useState, useEffect } from 'react';
import axios from 'axios';
import api from '../utils/api';
import {useTranslation} from "react-i18next";

export default function UserMenu() {
    const [userName, setUserName] = useState('');
    const [profilePicture, setProfilePicture] = useState('');

    const { t } = useTranslation();
    const [isMenuVisible, setIsMenuVisible] = useState(false);
    const [showLogoutConfirm, setShowLogoutConfirm] = useState(false);

    useEffect(() => {
        const storedName = localStorage.getItem('userName');
        const storedAvatar = localStorage.getItem('profilePicture');


        console.log('UserMenu useEffect: Reading from localStorage');
        console.log('UserMenu useEffect: storedName:', storedName);
        console.log('UserMenu useEffect: storedAvatar (profilePicture):', storedAvatar);


        if (storedName) {
            setUserName(storedName);
        }

        if (storedAvatar) {
            setProfilePicture(storedAvatar);
        }

        console.log('UserMenu useEffect: profilePicture state after setting:', storedAvatar);


    }, []);

    const toggleMenu = () => {
        setIsMenuVisible(!isMenuVisible);
    };

    const handleLogoutClick = () => {
        setShowLogoutConfirm(true);
    };

    const confirmLogout = async () => {
        try {
            await api.post('/auth/logout');
            localStorage.removeItem('userName');
            localStorage.removeItem('isAdmin');
            window.location.href = '/login';
        } catch (error) {
            console.error('Logout failed:', error);
        } finally {
            setShowLogoutConfirm(false);
        }
    };

    const cancelLogout = () => {
        setShowLogoutConfirm(false);
    };

    return (
        <div className="flex relative inline-block pr-6">
            <div className="relative text-sm">
                <button className="flex items-center focus:outline-none mr-3" onClick={toggleMenu}>
                    <span className="hidden md:inline-block">Hi, {userName }</span>
                    {profilePicture && (
                        <img

                            src={`http://localhost:5000${profilePicture}`}
                            alt="Avatar"
                            className="w-8 h-8 rounded-full ml-2 object-cover border border-gray-300"
                        />
                    )}
                    <svg className="pl-2 h-2" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 129 129">
                        <path d="m121.3,34.6c-1.6-1.6-4.2-1.6-5.8,0l-51,51.1-51.1-51.1c-1.6-1.6-4.2-1.6-5.8,0-1.6,1.6-1.6,4.2 0,5.8l53.9,53.9c0.8,0.8 1.8,1.2 2.9,1.2 1,0 2.1-0.4 2.9-1.2l53.9-53.9c1.7-1.6 1.7-4.2 0.1-5.8z"/>
                    </svg>
                </button>

                {isMenuVisible && (
                    <div className="bg-white nunito rounded shadow-md mt-2 absolute mt-12 top-15 right-0 min-w-full z-30">
                        <ul className="list-reset">
                            <li><a href="/profile" className="px-3 py-2 block text-gray-900 hover:bg-indigo-400 hover:text-white">{t('my_acc')}</a></li>
                            <li><hr className="border-t mx-2 border-gray-400" /></li>
                            <li><button
                                onClick={handleLogoutClick}
                                className="w-full text-left px-4 py-2 block text-gray-900 hover:bg-red-400 hover:text-white"
                            >
                                {t('logout')}
                            </button></li>
                        </ul>
                    </div>
                )}


                {showLogoutConfirm && (
                    <div className="fixed inset-0 bg-black bg-opacity-40 flex items-center justify-center z-50">
                        <div className="bg-white rounded-lg p-6 w-96 shadow-lg">
                            <h2 className="text-lg font-semibold mb-4">{t('logout_text')}</h2>
                            <div className="flex justify-end space-x-4">
                                <button
                                    onClick={cancelLogout}
                                    className="px-4 py-2 bg-gray-300 rounded hover:bg-gray-400"
                                >
                                    {t('no')}
                                </button>
                                <button
                                    onClick={confirmLogout}
                                    className="px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600"
                                >
                                    {t('yes')}
                                </button>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}
