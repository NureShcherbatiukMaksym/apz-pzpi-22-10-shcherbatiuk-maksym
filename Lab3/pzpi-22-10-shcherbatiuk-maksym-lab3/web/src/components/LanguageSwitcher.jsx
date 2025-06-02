import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';

export default function LanguageSwitcher() {
    const { i18n } = useTranslation();
    const [isEnglish, setIsEnglish] = useState(() => localStorage.getItem('language') !== 'uk');
    const toggleLanguage = () => {

        const newLang = i18n.language === 'en' ? 'uk' : 'en';

        i18n.changeLanguage(newLang);
        localStorage.setItem('language', newLang);

        setIsEnglish(newLang === 'en');
    };

    useEffect(() => {
        const storedLang = localStorage.getItem('language');
        const initialLang = storedLang || 'en';

        if (i18n.language !== initialLang) {
            i18n.changeLanguage(initialLang);
        }

        setIsEnglish(initialLang === 'en');
    }, [i18n]);


    return (

        <div className="flex flex-col items-center">
            <button
                onClick={toggleLanguage}

                className="flex items-center w-12 h-6 bg-gray-300 rounded-full px-1 border border-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-colors duration-300 ease-in-out"
            >
                <span
                    className={`w-4 h-4 bg-white rounded-full shadow-md transform duration-300 ease-in-out ${isEnglish ? 'translate-x-0' : 'translate-x-6'}`}
                ></span>
            </button>
            <div className="text-xs text-center mt-1">
                {i18n.language.toUpperCase()}
            </div>
        </div>
    );
}