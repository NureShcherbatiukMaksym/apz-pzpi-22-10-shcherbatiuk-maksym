import React, { useState } from 'react';
import AdminDashboard from '../components/AdminDashboard';
import AdminPanel from '../components/AdminPanel';

const AdminPage = () => {
    const [selectedCategory, setSelectedCategory] = useState(null);

    return (
        <div className=" w-full flex h-screen flex-wrap ">
            <AdminDashboard
                onSelectCategory={setSelectedCategory}
                selectedCategory={selectedCategory}
            />
            <AdminPanel selectedCategory={selectedCategory} />
        </div>
    );
};

export default AdminPage;
