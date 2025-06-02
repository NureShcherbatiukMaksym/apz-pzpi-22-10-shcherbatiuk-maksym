import React, { useState, useContext } from 'react';
import AuthForm from '../components/AuthForm';
import api from '../utils/api';
import { useNavigate } from 'react-router-dom';
import { signInWithPopup } from "firebase/auth";
import { auth, googleProvider } from "../firebase";
import { UserContext } from '../contexts/UserContext';
import LanguageSwitcher from '../components/LanguageSwitcher';


export default function RegisterPage() {
    const navigate = useNavigate();
    const { handleLoginSuccess } = useContext(UserContext);
    const [registerInProgress, setRegisterInProgress] = useState(false);
    const [formError, setFormError] = useState(null);


    const handleRegister = async ({ email, password, name }) => {
        setRegisterInProgress(true);
        setFormError(null);
        console.log('RegisterPage: Attempting password registration with data:', { email, password, name });

        try {
            const res = await api.post('/auth/register/password', { email, password, name });
            console.log('Password registration success:', res.data);
            const { user } = res.data;
            handleLoginSuccess(user);
            navigate('/');

        } catch (err) {
            console.error('Registration error:', err.response?.data || err.message);
            const errorMessage = err.response?.data?.message || 'Помилка реєстрації';
            setFormError(errorMessage);
            alert('Помилка реєстрації: ' + errorMessage);
        } finally {
            setRegisterInProgress(false);
        }
    };


    const handleGoogleRegister = async () => {
        setRegisterInProgress(true);
        setFormError(null);
        console.log('RegisterPage: Initiating Google sign-in popup for registration.');
        try {
            const result = await signInWithPopup(auth, googleProvider);
            const token = await result.user.getIdToken();

            console.log('RegisterPage: Sending Google token to backend /auth/register/google...');
            const res = await api.post('/auth/register/google', { token }, { withCredentials: true });

            console.log('Google registration success:', res.data);
            const { user } = res.data;
            handleLoginSuccess(user);
            navigate('/');

        } catch (error) {
            console.error('Google registration error:', error);
            const errorMessage = error.message || 'Помилка авторизації через Google';
            setFormError(errorMessage);
            alert('Помилка авторизації через Google: ' + errorMessage);
        } finally {
            setRegisterInProgress(false);
        }
    };

    const switchToLogin = () => {
        navigate('/login');
    };


    return (
        // Основний контейнер сторінки
        <div className="relative min-h-screen flex flex-col items-center justify-center py-12 px-4 sm:px-6 lg:px-8">

            <div className="max-w-sm w-full space-y-8 relative">

                <div className="absolute top-2 right-2 z-10">
                    <LanguageSwitcher />
                </div>

                {formError && <div className="text-red-500 text-center mb-4">{formError}</div>}

                <AuthForm
                    type="register"
                    onSubmit={handleRegister}
                    onGoogleLogin={handleGoogleRegister}
                    isSubmitting={registerInProgress}
                    onSwitchType={switchToLogin}
                />
            </div>
        </div>
    );
}