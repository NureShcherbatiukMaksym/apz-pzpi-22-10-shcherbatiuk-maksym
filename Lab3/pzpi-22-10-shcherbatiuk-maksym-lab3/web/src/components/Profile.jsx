import React, { useEffect, useState } from 'react';
import api from '../utils/api';
import { useUserContext } from '../contexts/UserContext';
import {useTranslation} from "react-i18next"; // Для оновлення контексту



const Profile = () => {

    const { user: userFromContext, handleLoginSuccess } = useUserContext();
    const { t } = useTranslation();

    const [user, setUser] = useState(null);

    const [name, setName] = useState("");
    const [email, setEmail] = useState("");

    const [hasPassword, setHasPassword] = useState(false);

    const [profilePicture, setProfilePicture] = useState(null);

    const [currentPassword, setCurrentPassword] = useState("");
    const [newPassword, setNewPasswordState] = useState("");
    const [confirmPassword, setConfirmPasswordState] = useState("");

    const [message, setMessage] = useState("");



    useEffect(() => {
        const fetchUser = async () => {
            try {
                console.log("Profile: Fetching user data for profile page...");

                const res = await api.get('/users/me');
                const userData = res.data.user || res.data;

                console.log("Profile: Fetched user data:", userData);


                setUser(userData);
                setName(userData.name || "");
                setEmail(userData.email || "");

                setHasPassword(!!userData.password);


            } catch (error) {
                console.error("Profile: Помилка завантаження профілю:", error.response?.data?.message || error.message);
                setMessage(error.response?.data?.message || "Помилка завантаження профілю.");
                setUser(null);
            }
        };

        fetchUser();


    }, []);


    const handleFileSelect = (e) => {
        const file = e.target.files ? e.target.files[0] : null;
        setProfilePicture(file);
        setMessage(""); // Очищаємо повідомлення
        console.log("Profile: File selected for upload:", file);
    }


    const handleSave = async (e) => {
        e.preventDefault();

        if (!user || !user.id) {
            setMessage("Помилка: Дані користувача не завантажено. Спробуйте оновити сторінку.");
            return;
        }

        if (!validateForm()) return;
        setMessage("Збереження...");

        try {
            const formData = new FormData();

            if (name !== user.name) formData.append("name", name);

            if (profilePicture) {
                console.log("Profile: Appending file to FormData with key 'profilePictureFile'.");
                formData.append("profile_picture", profilePicture);
            }

            if (currentPassword) formData.append("currentPassword", currentPassword);
            if (newPassword) formData.append("newPassword", newPassword);


            console.log(`Profile: Sending update request for user ${user.id} with FormData.`);
            const res = await api.put(`/users/${user.id}`, formData, {
                headers: { "Content-Type": "multipart/form-data" },
            });


            const updatedUserData = res.data.user || res.data;
            console.log("Profile: User updated successfully. Received updated data:", updatedUserData);

            setUser(updatedUserData);
            setName(updatedUserData.name || "");


            setMessage("Зміни успішно збережено.");

            if (handleLoginSuccess) {
                console.log("Profile: Calling handleLoginSuccess to update UserContext with updated data.");

                handleLoginSuccess(updatedUserData);
            } else {
                console.warn("Profile: handleLoginSuccess not available in context. UserMenu might not update.");

            }


            setCurrentPassword("");
            setNewPasswordState("");
            setConfirmPasswordState("");
            setProfilePicture(null);


        } catch (err) {
            console.error("Profile: Error saving profile:", err.response?.data?.message || err.message);
            setMessage(err.response?.data?.message || "Помилка збереження змін.");
        }
    };


    const handleNameChange = (e) => { setName(e.target.value); setMessage(""); }
    const handleCurrentPasswordChange = (e) => { setCurrentPassword(e.target.value); setMessage(""); }
    const handleNewPasswordChange = (e) => { setNewPasswordState(e.target.value); setMessage(""); }
    const handleConfirmPasswordChange = (e) => { setConfirmPasswordState(e.target.value); setMessage(""); }



    if (!user) {

        return <p>Завантаження даних профілю...</p>;
    }

    const validateForm = () => {
        setMessage("");

        if ((newPassword || confirmPassword) && !currentPassword) {
            setMessage("Будь ласка, введіть поточний пароль для зміни пароля.");
            return false;
        }

        if (newPassword && newPassword !== confirmPassword) {
            setMessage("Новий пароль та підтвердження пароля не співпадають.");
            return false;
        }

        return true;
    };

    return (
        <div className="w-full md:w-2/3 p-6">
            <h2 className="text-2xl font-semibold mb-4">{t('my_acc')}</h2>

            <form onSubmit={handleSave} className="space-y-4" encType="multipart/form-data">
                <div>
                    <label className="block font-medium text-gray-700 mb-1">{t('profile_photo')}</label>

                    <input
                        type="file"
                        accept="image/*"
                        onChange={handleFileSelect}
                        className="w-full"
                    />

                    {profilePicture ? (

                        <div className="mt-2">
                            <p className="text-sm text-gray-600">{t('preview_photo')}</p>
                            <img
                                src={URL.createObjectURL(profilePicture)}
                                alt={t('preview_photo')}
                                className="w-24 h-24 rounded-full object-cover border border-blue-500"
                            />

                        </div>
                    ) : (

                        user.profile_picture_url && (
                            <div className="mt-2">
                                <p className="text-sm text-gray-600">{t('current_photo')}</p>
                                <img

                                    src={`http://localhost:5000${user.profile_picture_url}`}
                                    alt={user?.name ? `${t('current_photo')} ${user.name}` : t('current_photo')}
                                    className="w-24 h-24 rounded-full object-cover border border-gray-300"
                                />
                                {/* TODO: Додати кнопку "Видалити фото профілю" */}
                            </div>
                        )
                    )}

                </div>


                <div>
                    <label className="block font-medium text-gray-700 mb-1">{t('email')}</label>
                    <input
                        type="email"
                        value={email}
                        disabled
                        className="w-full px-4 py-2 border border-gray-300 rounded bg-gray-100 cursor-not-allowed"
                    />
                </div>


                <div>
                    <label className="block font-medium text-gray-700 mb-1">{t('first_name')}</label>
                    <input
                        type="text"
                        value={name}
                        onChange={handleNameChange}
                        className="w-full px-4 py-2 border border-gray-300 rounded"
                    />
                </div>


                {user.password !== undefined && user.password !== null ? (
                    <>
                        <hr className="my-4" />
                        <h3 className="text-lg font-semibold">{t('password_change')}</h3>

                        <div>
                            <label className="block font-medium text-gray-700 mb-1">{t('current_password')}</label>
                            <input
                                type="password"
                                value={currentPassword}
                                onChange={handleCurrentPasswordChange}
                                className="w-full px-4 py-2 border border-gray-300 rounded"
                            />
                        </div>

                        <div>
                            <label className="block font-medium text-gray-700 mb-1">{t('new_password')}</label>
                            <input
                                type="password"
                                value={newPassword}
                                onChange={handleNewPasswordChange}
                                className="w-full px-4 py-2 border border-gray-300 rounded"
                            />
                        </div>

                        <div>
                            <label className="block font-medium text-gray-700 mb-1">{t('confirm_password')}</label>
                            <input
                                type="password"
                                value={confirmPassword}
                                onChange={handleConfirmPasswordChange}
                                className="w-full px-4 py-2 border border-gray-300 rounded"
                            />
                        </div>
                    </>
                ) : (

                    <div className="mt-4 text-gray-600">
                        <p>{t('password_message')}</p> {/* TODO: Реалізувати функціонал додавання пароля */}
                    </div>
                )}


                {message && <p className="text-sm text-blue-600">{message}</p>}

                <button
                    type="submit"
                    className="bg-indigo-600 text-white px-4 py-2 rounded hover:bg-indigo-700 transition"
                >
                    {t('save_changes')}
                </button>
            </form>
        </div>
    );
};

export default Profile;