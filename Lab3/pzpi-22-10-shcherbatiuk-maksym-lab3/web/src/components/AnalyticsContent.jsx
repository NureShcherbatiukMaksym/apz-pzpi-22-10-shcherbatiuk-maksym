import React from 'react';

const AnalyticsContent = () => {
    return (
        <div id="main-content" className="w-full flex-1">
            <div className="flex flex-1 flex-wrap">
                <div className="w-full xl:w-2/3 p-6 xl:max-w-6xl">
                    <div className="max-w-full lg:max-w-3xl xl:max-w-5xl">
                        <div className="border-b p-3">
                            <h5 className="font-bold text-black">Graph</h5>
                        </div>
                        <div className="p-5">
                            <div className="ct-chart ct-golden-section" id="chart1"></div>
                        </div>
                        <div className="p-3">
                            <div className="border-b p-3">
                                <h5 className="font-bold text-black">Table</h5>
                            </div>
                            <div className="p-5">
                                <table className="w-full p-5 text-gray-700">
                                    <thead>
                                    <tr>
                                        <th className="text-left text-blue-900">Name</th>
                                        <th className="text-left text-blue-900">Side</th>
                                        <th className="text-left text-blue-900">Role</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <tr>
                                        <td>Obi Wan Kenobi</td>
                                        <td>Light</td>
                                        <td>Jedi</td>
                                    </tr>
                                    <tr>
                                        <td>Greedo</td>
                                        <td>South</td>
                                        <td>Scumbag</td>
                                    </tr>
                                    <tr>
                                        <td>Darth Vader</td>
                                        <td>Dark</td>
                                        <td>Sith</td>
                                    </tr>
                                    </tbody>
                                </table>
                                <p className="py-2"><a href="#">See More issues...</a></p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default AnalyticsContent;
