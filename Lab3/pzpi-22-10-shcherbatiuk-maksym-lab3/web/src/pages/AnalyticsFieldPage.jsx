import React from 'react';
import DashboardContent from '../components/DashboardContent';
import AnalyticsField from "../components/AnalyticsField";

const FieldPage = () => {
    return (
        <div className="flex flex-wrap">
            <DashboardContent />
            <AnalyticsField />
        </div>
    );
}

export default FieldPage;
