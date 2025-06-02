import React, { useState } from 'react';
import {useTranslation} from "react-i18next";

export default function AuthForm({ type, onSubmit, onGoogleLogin, isSubmitting, onSwitchType }) {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [name, setName] = useState('');
    const { t } = useTranslation();

    const handleSubmit = e => {
        e.preventDefault();
        const formData = {
            email,
            password,
        };
        if (type === 'register') {
            formData.name = name;
        }
        onSubmit(formData);
    };

    return (
        <form onSubmit={handleSubmit} className="flex flex-col gap-4 max-w-sm mx-auto mt-10">
            <h2 className="text-2xl font-bold text-center">
                {type === 'login' ? t('login_title') : t('register_title')}
            </h2>

            {type === 'register' && (
                <input
                    type="text"
                    placeholder={t('first_name')}
                    value={name}
                    onChange={e => setName(e.target.value)}
                    className="border p-2 rounded"
                    required
                />
            )}

            <input
                type="email"
                placeholder={t('email')}
                value={email}
                onChange={e => setEmail(e.target.value)}
                className="border p-2 rounded"
                required
            />
            <input
                type="password"
                placeholder={t('password')}
                value={password}
                onChange={e => setPassword(e.target.value)}
                className="border p-2 rounded"
                required
            />

            <button type="submit" className="bg-blue-500 text-white py-2 rounded hover:bg-blue-600 disabled:opacity-50" disabled={isSubmitting}>
                {/* Використовуємо переклад та стан завантаження */}
                {type === 'login' ? (isSubmitting ? t('logging_in') : t('login')) : (isSubmitting ? t('registering') : t('register'))}
            </button>

            <button
                type="button"
                onClick={onGoogleLogin}
                className="bg-red-500 hover:bg-red-600 text-white py-2 rounded disabled:opacity-50"
                disabled={isSubmitting}
            >
                {type === 'login' ? t('sign_in_google') : t('sign_up_google')}
            </button>

            <button
                type="button"
                onClick={onSwitchType}
                className="mt-4 text-center text-blue-500 hover:underline"
            >

                {type === 'login' ? t('register') : t('login')}
            </button>

        </form>
    );
}