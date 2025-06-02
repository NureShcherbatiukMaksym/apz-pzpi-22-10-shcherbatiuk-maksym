// src/contexts/FieldContext.js
import { createContext, useContext, useState } from 'react';

const FieldContext = createContext();

export const useFieldContext = () => useContext(FieldContext);

export const FieldProvider = ({ children }) => {
    const [sortBy, setSortBy] = useState('name');
    const [sortOrder, setSortOrder] = useState('asc');
    const [filter, setFilter] = useState('all');
    const [searchTerm, setSearchTerm] = useState(''); // додали

    return (
        <FieldContext.Provider value={{
            sortBy,
            setSortBy,
            sortOrder,
            setSortOrder,
            filter,
            setFilter,
            searchTerm,
            setSearchTerm
        }}>
            {children}
        </FieldContext.Provider>
    );
};
