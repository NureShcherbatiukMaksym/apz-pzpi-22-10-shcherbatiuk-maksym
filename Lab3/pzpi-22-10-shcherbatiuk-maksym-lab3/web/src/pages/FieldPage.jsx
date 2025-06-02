import React from 'react';
import DashboardContent from '../components/DashboardContent';
import Field  from '../components/Field.jsx';

const FieldPage = () => {
    return (
        <div className="flex flex-wrap">
            <DashboardContent />
            <Field />
        </div>
    );
}

export default FieldPage;
