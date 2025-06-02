import React from 'react';
import { useTranslation } from "react-i18next";

const AdminDashboard = ({ onSelectCategory, selectedCategory }) => {
    const { t } = useTranslation();

    const categories = [
        { id: 'users', label: t(`users`), icon: 'fa-user' },
        { id: 'devices', label: t(`devices`), icon: 'fa-microchip' },
        { id: 'sensors', label: t(`sensors`), icon: 'fa-thermometer-half' },
        { id: 'userDevices', label: t(`users_devices`), icon: 'fa-link' },
    ];

    return (
        <div className="bg-gray-200 w-full lg:max-w-sm p-4 border-r  overflow-y-auto">
            <h2 className="text-2xl font-bold mb-4">{t(`categories`)}</h2>
            <div className="flex flex-col gap-4 pb-4">
                {categories.map(cat => (
                    <div
                        key={cat.id}
                        onClick={() => onSelectCategory(cat.id)}
                        className={`cursor-pointer border border-gray-300 rounded-lg p-4 transition 
                            ${selectedCategory === cat.id ? 'bg-indigo-100 border-indigo-500' : 'hover:bg-indigo-50'}`}
                    >
                        <div className="flex items-center gap-3">
                            <i className={`fas ${cat.icon} text-indigo-500`}></i>
                            <span className="font-medium">{cat.label}</span>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default AdminDashboard;
