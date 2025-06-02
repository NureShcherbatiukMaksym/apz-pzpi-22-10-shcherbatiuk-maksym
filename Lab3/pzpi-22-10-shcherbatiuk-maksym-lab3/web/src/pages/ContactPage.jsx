import React from 'react';

const ContactPage = () => {
    return (
        <div className="p-6 max-w-4xl mx-auto">
            <h1 className="text-4xl font-bold mb-6 text-center">Контакти</h1>

            <div className="space-y-6 text-gray-700">
                <div className="flex items-center">
                    <i className="fas fa-phone-alt text-xl mr-4 text-blue-600"></i>
                    <span>+380 (50) 123-45-67</span>
                </div>
                <div className="flex items-center">
                    <i className="fas fa-envelope text-xl mr-4 text-green-600"></i>
                    <span>info@soilscout.ua</span>
                </div>
                <div className="flex items-center">
                    <i className="fas fa-map-marker-alt text-xl mr-4 text-red-600"></i>
                    <span>Київ, Україна</span>
                </div>
            </div>
        </div>
    );
};

export default ContactPage;
