import React, { useState, useContext } from 'react';
import api from '../utils/api';
import { useNavigate } from 'react-router-dom';
import { auth, googleProvider } from '../firebase';
import { signInWithPopup } from 'firebase/auth';
import { UserContext } from '../contexts/UserContext';
import AuthForm from '../components/AuthForm';
import LanguageSwitcher from '../components/LanguageSwitcher';

export default function LoginPage() {
    const navigate = useNavigate();
    const { user, handleLoginSuccess } = useContext(UserContext);

    const [loginInProgress, setLoginInProgress] = useState(false);
    const [formError, setFormError] = useState(null);


    if (user) {
        console.log('LoginPage: User is already logged in, navigating to /.');
        navigate('/', { replace: true });
        return null;
    }

    const handleLogin = async ({ email, password }) => {
        setLoginInProgress(true);
        setFormError(null); // Скидаємо помилку перед спробою
        console.log('LoginPage: Attempting password login with data:', { email, password });
        try {
            const res = await api.post('/auth/login/password', { email, password }, {
                withCredentials: true
            });
            console.log('LoginPage: Password login success. Response received.');

            const { user } = res.data;

            handleLoginSuccess(user);


            navigate('/');

        } catch (err) {
            console.error('LoginPage: Password login error:', err.response?.data || err.message);
            const errorMessage = err.response?.data?.message || 'Помилка входу';
            setFormError(errorMessage);
            alert('Помилка входу: ' + errorMessage);
        } finally {
            setLoginInProgress(false);
        }
    };

    const handleGoogleLogin = async () => {
        setLoginInProgress(true);
        setFormError(null); // Скидаємо помилку перед спробою
        console.log('LoginPage: Initiating Google sign-in popup.');
        try {
            const result = await signInWithPopup(auth, googleProvider);
            const token = await result.user.getIdToken();
            console.log('LoginPage: Firebase Google auth success. Token obtained.');

            console.log('LoginPage: Sending Google token to backend /auth/login/google...');
            const res = await api.post('/auth/login/google', { token }, { withCredentials: true });
            console.log('LoginPage: Backend Google login success. Response received.', res.data);

            const { user } = res.data;


            handleLoginSuccess(user);

            navigate('/');

        } catch (error) {
            console.error('LoginPage: Google login error:', error);
            const errorMessage = error.message || 'Помилка авторизації через Google';
            setFormError(errorMessage); // Встановлюємо помилку форми
            alert('Помилка авторизації через Google: ' + errorMessage); // Також показуємо alert
        } finally {
            setLoginInProgress(false);
        }
    };

    const switchToRegister = () => {
        navigate('/register');
    };


    return (

        <div className="min-h-screen flex flex-col items-center justify-center py-12 px-4 sm:px-6 lg:px-8">

            <div className="max-w-sm w-full space-y-8 relative">

                <div className="absolute top-2 right-2 z-10">
                    <LanguageSwitcher />
                </div>

                {formError && <div className="text-red-500 text-center mb-4">{formError}</div>}

                <AuthForm
                    type="login"
                    onSubmit={handleLogin}
                    onGoogleLogin={handleGoogleLogin}
                    isSubmitting={loginInProgress}
                    onSwitchType={switchToRegister}
                />
            </div>
        </div>
    );
}