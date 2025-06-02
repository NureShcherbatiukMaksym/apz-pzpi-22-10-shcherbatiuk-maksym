import { useState, useRef, useEffect } from "react";
import { useNavigate } from 'react-router-dom';
import UserMenu from "./UserMenu";
import { useFieldContext } from '../contexts/FieldContext';
import { useTranslation } from 'react-i18next';

export default function Navbar({ setIsCreatingField }) {
    const {
        sortBy, setSortBy,
        sortOrder, setSortOrder,
        filter, setFilter,
        setSearchTerm
    } = useFieldContext();

    const { t, i18n } = useTranslation();
    const [isEnglish, setIsEnglish] = useState(true);

    const toggleLanguage = () => {
        const newLang = isEnglish ? 'en' : 'uk';
        i18n.changeLanguage(newLang);
        localStorage.setItem('language', newLang);
        setIsEnglish(!isEnglish);
    };


    useEffect(() => {
        const storedLang = localStorage.getItem('language');
        const initialLang = storedLang || 'en';
        i18n.changeLanguage(initialLang);
        setIsEnglish(initialLang === 'uk');
    }, []);


    const [isFilterMenuOpen, setIsFilterMenuOpen] = useState(false);
    const [isDropdownOpen, setIsDropdownOpen] = useState(false);

    const [sortaOrders, setSortOrders] = useState({
        name: "asc",
        area: "asc",
        created_at: "asc"
    });

    const filterRef = useRef(null);
    const dropdownRef = useRef(null);
    const navigate = useNavigate();

    const handleAddField = () => {
        navigate('/create-field');
    };
    const handleSetSortOrder = (order) => {
        setSortOrder(order);
        setSortOrders(prevOrders => ({
            ...prevOrders,
            [sortBy]: order
        }));
    };
    const handleSortOrderToggle = () => {
        const newOrder = sortOrder === "asc" ? "desc" : "asc";
        setSortOrder(newOrder);
        setSortOrders(prev => ({
            ...prev,
            [sortBy]: newOrder
        }));
    };

    const handleSortByChange = (newSortBy) => {
        if (newSortBy === sortBy) {
            return;
        }
        setSortBy(newSortBy);
        setIsDropdownOpen(false);
    };


    const handleSortToggle = () => {

        const newSortOrder = sortOrder === "asc" ? "desc" : "asc";
        setSortOrder(newSortOrder);
        setSortOrders(prevOrders => ({
            ...prevOrders,
            [sortBy]: newSortOrder
        }));
    };

    const handleFilterChange = (newFilter) => {
        setFilter(newFilter);
        setIsFilterMenuOpen(false);
    };

    const toggleFilterMenu = () => {
        setIsFilterMenuOpen((prev) => !prev);
    };

    const toggleDropdown = () => {
        setIsDropdownOpen((prev) => !prev);
    };

    useEffect(() => {
        const handleClickOutside = (e) => {
            if (filterRef.current && !filterRef.current.contains(e.target)) {
                setIsFilterMenuOpen(false);
            }

            if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
                setIsDropdownOpen(false);
            }
        };

        document.addEventListener("mousedown", handleClickOutside);
        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
        };
    }, []);


    return (
        <div className="h-40 lg:h-20 w-full flex flex-wrap">
            <nav className="bg-gray-200 w-full lg:max-w-sm flex items-center border-b border-gray-300 order-2 lg:order-1">
                <div className="flex w-full p-3 gap-2 items-center">
                    <button
                        onClick={handleAddField}
                        id = "add-field"
                        className="bg-green-500 hover:bg-green-600 text-white px-2 py-2 rounded-full font-semibold"
                    >
                        <i className="fas fa-plus mr-1" />
                        {t('add_field')}
                    </button>

                    <div className="flex items-center gap-2 relative" ref={dropdownRef}>
                        <div className="relative flex items-center gap-1">
                            <button
                                onClick={toggleDropdown}
                                id = "sorting"
                                className="bg-gray-300 hover:bg-gray-400 text-gray-700 px-2 py-2 rounded-full"
                            >
                                {sortBy === "name" && t("field_name")}
                                {sortBy === "area" && t("area")}
                                {sortBy === "created_at" && t("created_at")}

                            </button>

                            <button
                                onClick={handleSortToggle}
                                className="bg-gray-300 hover:bg-gray-400 text-gray-700 px-2 py-2 rounded-full"
                            >
                                {sortOrder === "asc" ? (
                                    <i className="fas fa-arrow-up" />
                                ) : (
                                    <i className="fas fa-arrow-down" />
                                )}
                            </button>
                        </div>

                        {isDropdownOpen && (
                            <div className="absolute bg-white shadow-md rounded-lg mt-1 p-2 w-48 border top-10 z-50">
                                <ul>
                                    <li>
                                        <button
                                            onClick={() => handleSortByChange("name")}
                                            className="w-full text-left px-4 py-2 hover:bg-gray-200"
                                        >
                                            {t('field_name')}
                                        </button>
                                    </li>
                                    <li>
                                        <button
                                            onClick={() => handleSortByChange("area")}
                                            className="w-full text-left px-4 py-2 hover:bg-gray-200"
                                        >
                                            {t('area')}
                                        </button>
                                    </li>
                                    <li>
                                        <button
                                            onClick={() => handleSortByChange("created_at")}
                                            className="w-full text-left px-4 py-2 hover:bg-gray-200"
                                        >
                                            {t('created_at')}
                                        </button>
                                    </li>
                                </ul>
                            </div>
                        )}
                    </div>

                    <div className=" relative ml-auto" ref={filterRef}>
                        <button
                            onClick={toggleFilterMenu}
                            className="bg-blue-500 hover:bg-blue-600 text-white px-3 py-2 rounded-full "

                        >
                            <i className="fas fa-filter" />
                        </button>

                        {isFilterMenuOpen && (
                            <div className="absolute bg-white shadow-md rounded-lg mt-2 p-2 w-48 border top-10 z-50">
                                <ul>
                                    <li>
                                        <button
                                            onClick={() => handleFilterChange("all")}
                                            className="w-full text-left px-4 py-2 hover:bg-gray-200"
                                        >
                                            {t('all')}
                                        </button>
                                    </li>
                                    <li>
                                        <button
                                            onClick={() => handleFilterChange("active")}
                                            className="w-full text-left px-4 py-2 hover:bg-gray-200"
                                        >
                                            {t('active')}
                                        </button>
                                    </li>
                                    <li>
                                        <button
                                            onClick={() => handleFilterChange("inactive")}
                                            className="w-full text-left px-4 py-2 hover:bg-gray-200"
                                        >
                                            {t('inactive')}
                                        </button>
                                    </li>
                                </ul>
                            </div>
                        )}
                    </div>
                </div>
            </nav>

            <nav className="bg-gray-100 w-auto flex-1 border-b border-gray-300 order-1 lg:order-2">
                <div className="flex h-full justify-between items-center">
                    <div className="relative w-full max-w-3xl px-6 py-2  ">
                        <div className="absolute h-10 mt-1 left-0 top-0 flex items-center pl-10 pt-4">
                            <i className="fas fa-search text-gray-600" />
                        </div>
                        <input
                            type="search"
                            placeholder={t('search_field')}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="block w-full bg-gray-200 text-gray-700 font-bold rounded-full pl-12 pr-4 py-3 focus:outline-none focus:bg-white focus:shadow-md"
                        />
                    </div>
                    <div className="relative">
                        <button
                            onClick={toggleLanguage}
                            className="flex items-center justify-between w-12 h-6 bg-gray-300 rounded-full px-1 border border-gray-400"
                        >
                            <span
                                className={`w-4 h-4 bg-white rounded-full shadow-md transform duration-300 ${isEnglish ? 'translate-x-6' : 'translate-x-0'}`}
                            ></span>
                        </button>
                        <div className="text-xs text-center mt-1">
                            {isEnglish ? 'UK' : 'EN'}
                        </div>
                    </div>
                    <UserMenu />
                </div>
            </nav>
        </div>
    );
}