import React from 'react';
import AdminUsers from './categories/AdminUsers';
import AdminDevices from './categories/AdminDevices';
import AdminSensors from './categories/AdminSensors';
import AdminUserDevices from './categories/AdminUserDevices';
import {useTranslation} from "react-i18next";

const AdminPanel = ({ selectedCategory }) => {
    const { t, i18n } = useTranslation();
    const renderContent = () => {
        switch (selectedCategory) {
            case 'users': return <AdminUsers />;
            case 'devices': return <AdminDevices />;
            case 'sensors': return <AdminSensors />;
            case 'userDevices': return <AdminUserDevices />;
            default: return <p className="text-gray-500">{t(`select_categories`)}</p>;
        }
    };

    return (
        <div className="flex flex-wrap flex-1 p-4 overflow-y-auto">
            {renderContent()}
        </div>
    );
};

export default AdminPanel;
