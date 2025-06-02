import React, { useEffect, useState } from 'react';
import { useNavigate, useLocation } from "react-router-dom";
import api from '../utils/api';
import { useFieldContext } from '../contexts/FieldContext';
import {useTranslation} from "react-i18next";


const DashboardContent = () => {
    const { filter, sortBy, sortOrder, searchTerm } = useFieldContext();
    const { t } = useTranslation();
    const [fields, setFields] = useState([]);
    const navigate = useNavigate();
    const location = useLocation();

    useEffect(() => {
        api.get('/fields')
            .then(response => setFields(response.data))
            .catch(error => console.error('Error fetching fields:', error));
    }, [sortBy, sortOrder, filter]);


    const searchedFields = fields.filter(field =>
        field.name.toLowerCase().includes(searchTerm.toLowerCase())
    );

    const filteredFields = searchedFields.filter(field => {
        if (filter === "all") return true;
        if (filter === "active") return field.selected === true;
        if (filter === "inactive") return field.selected === false;
        return true;
    });



    const sortedFields = [...filteredFields].sort((a, b) => {
        if (sortBy === "name") {
            return sortOrder === "asc" ? a.name.localeCompare(b.name) : b.name.localeCompare(a.name);
        }
        if (sortBy === "area") {
            return sortOrder === "asc" ? a.area - b.area : b.area - a.area;
        }
        if (sortBy === "created_at") {
            return sortOrder === "asc"
                ? new Date(a.created_at) - new Date(b.created_at)
                : new Date(b.created_at) - new Date(a.created_at);
        }
        return 0;
    });


    const getFieldNavigationPath = (fieldId) => {
        if (location.pathname.startsWith('/analytics')) {

            return `/analytics/field/${fieldId}`;
        }

        return `/home/field/${fieldId}`;
    };


    return (
        <div id="dash-content" className="bg-gray-200 py-6 lg:py-0 w-full lg:max-w-sm flex flex-wrap content-start overflow-y-auto max-h-screen">
            {fields.length === 0 && (
                <p className="text-center w-full text-gray-500">{t(`fetch_field_exceptions`)}</p>
            )}
            {sortedFields.map(field => (
                <div key={field.id} className="w-1/2 lg:w-full">
                    <div
                        onClick={() => navigate(getFieldNavigationPath(field.id))}
                        className="cursor-pointer border-2 border-gray-400 border-dashed hover:border-transparent hover:bg-white hover:shadow-xl rounded p-6 m-2 md:mx-10 md:my-6 relative transition duration-200"
                    >


                        <button
                            onClick={(e) => {
                                e.stopPropagation(); // блокуємо основний клік

                                navigate(`/field/${field.id}/edit`);
                            }}
                            className="absolute top-3 right-3 text-gray-500 hover:text-indigo-600"
                            title="Редагувати поле"
                        >
                            <i className="fas fa-edit text-xl"></i>
                        </button>


                        <div className="flex flex-col items-center">
                            <div className="flex-shrink ">
                                <div className="rounded-full p-3 bg-gray-300">
                                    <i className="fa fa-seedling fa-fw fa-inverse text-indigo-500"></i>
                                </div>
                            </div>
                            <div className="flex-1 text-center">
                                <h3 className="font-bold text-2xl">{field.name}</h3>
                                <h5 className="font-bold text-gray-500">{field.description}</h5>
                            </div>
                        </div>
                    </div>
                </div>
            ))}
        </div>
    );
};

export default DashboardContent;