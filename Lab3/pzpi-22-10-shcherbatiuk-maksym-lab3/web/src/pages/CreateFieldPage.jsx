import React from 'react';
import DashboardContent from '../components/DashboardContent';
import CreateField  from '../components/CreateField.jsx';

const CreateFieldPage = () => {
    return (
        <div className="flex flex-wrap">
            <DashboardContent />
            <CreateField  />
        </div>
    );
}

export default CreateFieldPage;
