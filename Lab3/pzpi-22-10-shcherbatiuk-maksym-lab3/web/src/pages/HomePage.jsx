import React from 'react';
import DashboardContent from '../components/DashboardContent';
import MainContent from '../components/MainContent.jsx';

const HomePage = () => {
    return (
        <div className="flex flex-wrap">
            <DashboardContent />
            <MainContent />
        </div>
    );
}

export default HomePage;
