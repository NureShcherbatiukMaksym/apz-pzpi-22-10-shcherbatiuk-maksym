
import React, { useContext } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { UserContext } from '../contexts/UserContext';

const ProtectedRoute = ({ children }) => {

    const { user, isAdmin, hasDevice, loadingInitial, loadingDevice } = useContext(UserContext);
    const location = useLocation();


    if (loadingInitial || loadingDevice) {

        console.log('ProtectedRoute: Loading user or device status...');
        return <div>Завантаження статусу користувача...</div>;
    }

    if (!user) {
        console.log('ProtectedRoute: Access denied (user is null). Redirecting to /login.');

        return <Navigate to="/login" state={{ from: location.pathname }} replace />;
    }

    if (isAdmin) {
        console.log('ProtectedRoute: User is admin. Access granted.');
        return children;
    }

    if (!hasDevice) {
        console.log('ProtectedRoute: User is not admin AND has no device. Redirecting to /buy-device.');

        return <Navigate to="/buy-device" state={{ reason: 'no-device', from: location.pathname }} replace />;
    }

    console.log('ProtectedRoute: Access granted.');
    return children;
};

export default ProtectedRoute;