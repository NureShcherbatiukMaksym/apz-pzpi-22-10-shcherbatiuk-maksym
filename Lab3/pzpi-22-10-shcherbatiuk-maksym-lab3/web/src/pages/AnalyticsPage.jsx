import React from 'react';
import DashboardContent from "../components/DashboardContent";
import AnalyticsContent from "../components/AnalyticsContent";
const AnalyticsPage = () => {
    return (
        <div className="flex flex-wrap">
            <DashboardContent />
            <AnalyticsContent />
        </div>
    );
}

export default AnalyticsPage;
