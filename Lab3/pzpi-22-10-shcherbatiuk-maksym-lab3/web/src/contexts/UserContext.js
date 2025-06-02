// contexts/UserContext.js
import React, { createContext, useState, useEffect, useCallback, useRef, useContext } from 'react';
import api, { setUnauthorizedCallback } from '../utils/api';

export const useUserContext = () => {
    const context = useContext(UserContext);
    if (context === undefined) {
        throw new Error('useUserContext must be used within a UserProvider');
    }
    return context;
};

export const UserContext = createContext();

export const UserProvider = ({ children }) => {
    const [user, setUser] = useState(() => {
        const storedUserName = localStorage.getItem('userName');
        const storedProfilePictureUrl = localStorage.getItem('profilePicture');
        return storedUserName ? {
            name: storedUserName,
            profile_picture_url: storedProfilePictureUrl || null,
        } : null;
    });
    const [isAdmin, setIsAdmin] = useState(() => localStorage.getItem('isAdmin') === 'true');
    const [hasDevice, setHasDevice] = useState(false);
    const [loadingDevice, setLoadingDevice] = useState(false);
    const [loadingInitial, setLoadingInitial] = useState(true);
    const [error, setError] = useState(null);

    const isInitialCheckDone = useRef(false);

    const checkDeviceStatus = useCallback(async (currentUser) => {
        if (!currentUser || loadingDevice) {
            if (!currentUser) console.log('Context: checkDeviceStatus skipped - user is null.');
            if (loadingDevice) console.log('Context: checkDeviceStatus skipped - already checking.');
            setHasDevice(false);
            return;
        }

        setLoadingDevice(true);
        console.log('Context: Performing device status check...');
        try {
            const devicesRes = await api.get('/user-iot-devices');
            const userHasDevice = devicesRes.data && devicesRes.data.length > 0;
            setHasDevice(userHasDevice);
            console.log(`Context: Device check successful. Has device: ${userHasDevice}.`);
        } catch (err) {
            console.error('Context: Failed to fetch user devices:', err);
            if (err.response?.status !== 401) {
                setHasDevice(false);
            }
        } finally {
            setLoadingDevice(false);
        }
    }, [loadingDevice]);

    useEffect(() => {
        if (isInitialCheckDone.current) {
            console.log('Context: Initial check already done, skipping useEffect.');
            return;
        }

        isInitialCheckDone.current = true;
        console.log('Context: Starting initial check useEffect.');

        const storedUserName = localStorage.getItem('userName');
        const storedProfilePictureUrl = localStorage.getItem('profilePicture');
        const initialUser = storedUserName ? {
            name: storedUserName,
            profile_picture_url: storedProfilePictureUrl || null,
        } : null;

        setUser(initialUser);
        setIsAdmin(localStorage.getItem('isAdmin') === 'true');

        if (initialUser) {
            checkDeviceStatus(initialUser);
        } else {
            setHasDevice(false);
        }

        setLoadingInitial(false);
        console.log('Context: Initial check useEffect finished.');

    }, [checkDeviceStatus]);

    const logoutUser = useCallback(async () => {
        console.log('Context: logoutUser called.');
        try {
            console.log('Context: Attempting backend /auth/logout call (optional)...');
            // await api.post('/auth/logout');
            console.log('Context: Backend logout call finished (or skipped).');
        } catch (err) {
            console.error('Context: Failed to call backend logout API:', err);
        } finally {
            console.log('Context: Performing local logout cleanup.');
            setUser(null);
            setIsAdmin(false);
            setHasDevice(false);
            setLoadingDevice(false);
            localStorage.removeItem('userName');
            localStorage.removeItem('isAdmin');
            localStorage.removeItem('profilePicture');

        }
    }, []);

    useEffect(() => {
        console.log('Context: Setting API unauthorized callback.');
        setUnauthorizedCallback(logoutUser);

        return () => {
            console.log('Context: Clearing API unauthorized callback.');
            setUnauthorizedCallback(null);
        };
    }, [logoutUser]);


    const handleLoginSuccess = useCallback((userDataFromLogin) => {
        console.log('Context: handleLoginSuccess called.');

        setUser(userDataFromLogin);

        setIsAdmin(userDataFromLogin.is_admin || false);
        setError(null);

        localStorage.setItem('userName', userDataFromLogin.name || "");
        localStorage.setItem('isAdmin', userDataFromLogin.is_admin ? 'true' : 'false');
        if (userDataFromLogin.profile_picture_url) {
            localStorage.setItem('profilePicture', userDataFromLogin.profile_picture_url);
            console.log('Context: profilePicture saved to localStorage:', userDataFromLogin.profile_picture_url);
        } else {
            localStorage.removeItem('profilePicture');
            console.log('Context: No profile picture URL received, removed from localStorage.');
        }


        checkDeviceStatus(userDataFromLogin);

    }, [checkDeviceStatus]);


    const contextValue = {
        user,
        isAdmin,
        hasDevice,
        loadingInitial,
        loadingDevice,
        error,
        logoutUser,
        handleLoginSuccess,
        checkDeviceStatus,
    };

    return (
        <UserContext.Provider value={contextValue}>
            {children}
        </UserContext.Provider>
    );
};