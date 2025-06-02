import React, { useEffect, useState, useRef, useCallback } from 'react';
import { useParams } from 'react-router-dom';
import api from '../utils/api';
import Chartist from 'chartist';
import 'chartist/dist/chartist.min.css';
import { useTranslation } from "react-i18next";

const AnalyticsField = () => {
    const { id } = useParams();
    const [timeRange, setTimeRange] = useState('7d');
    const [chartData, setChartData] = useState(null);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(null);
    const { t } = useTranslation();
    const chartContainerRef = useRef(null);
    const [isFieldSelectedAndReady, setIsFieldSelectedAndReady] = useState(false);

    const selectField = useCallback(async (fieldId) => {
        if (!fieldId) return;
        try {
            await api.post('/fields/select/', { fieldId });
        } catch (err) {
            console.error('Помилка при виборі поля:', err);
            throw err;
        }
    }, []);

    const deselectField = useCallback(async (fieldId) => {
        if (!fieldId) return;
        try {
            await api.post('/fields/deselect/', { fieldId });
        } catch (err) {
            console.error('Помилка при деселекті поля:', err);
        }
    }, []);

    const fetchData = useCallback(async () => {
        if (!id) {
            console.log('fetchData: No ID, skipping fetch.');
            setChartData(null);
            setError(null);
            return;
        }

        console.log(`WorkspaceData: Fetching data for field ${id} and timeRange ${timeRange}`);
        setIsLoading(true);
        setError(null);

        try {
            const response = await api.get(`/field-measurements/chart/${timeRange}`);
            console.log('fetchData: API response received:', JSON.stringify(response.data, null, 2));
            if (response.data && response.data.aggregatedData) {
                setChartData(response.data);
            } else {
                console.warn('fetchData: API response missing aggregatedData.');
                setChartData({ aggregatedData: [] });
                setError('Дані отримано, але вони порожні або у невірному форматі.');
            }
        } catch (err) {
            console.error('fetchData: Помилка при завантаженні аналітики:', err);
            setError(t('failed_load_data'));
            setChartData(null);
        } finally {
            setIsLoading(false);
        }
    }, [id, timeRange]);

    useEffect(() => {
        if (!id) {
            console.log('Selection useEffect: No ID.');
            setIsFieldSelectedAndReady(false);
            setChartData(null);
            setError(null);
            return;
        }
        console.log(`Selection useEffect: ID changed to ${id}. Starting selection process.`);

        const doSelect = async () => {
            setIsLoading(true);
            setError(null);
            setIsFieldSelectedAndReady(false);
            console.log(`doSelect: Selecting field ${id}`);
            try {
                await selectField(id);
                console.log(`doSelect: Field ${id} selected successfully.`);
                setIsFieldSelectedAndReady(true);
            } catch (err) {
                console.error(`doSelect: Помилка при виборі поля ${id}:`, err);
                setError('Не вдалося вибрати поле.');
                setIsFieldSelectedAndReady(false);
                setChartData(null);
            } finally {
                setIsLoading(false);
            }
        };
        doSelect();
    }, [id, selectField, deselectField]);


    useEffect(() => {
        console.log(`Data fetching trigger useEffect: id=${id}, isFieldSelectedAndReady=${isFieldSelectedAndReady}, timeRange=${timeRange}`);
        if (id && isFieldSelectedAndReady) {
            console.log('Data fetching trigger useEffect: Conditions met, calling fetchData.');
            fetchData();
        } else if (!id) {
            console.log('Data fetching trigger useEffect: No ID, clearing chartData.');
            setChartData(null);
            setError(null);
        } else {
            console.log('Data fetching trigger useEffect: Conditions not met (field not ready or no ID).');
        }
    }, [id, isFieldSelectedAndReady, timeRange, fetchData]);


    useEffect(() => {
        console.log('Chart rendering effect triggered. chartData:', JSON.stringify(chartData, null, 2));

        const clearChartContainer = (elementId) => {
            const element = document.getElementById(elementId);
            if (element) {
                element.innerHTML = '';
            } else {
                console.warn(`Chart container #${elementId} not found for clearing.`);
            }
            return element;
        };

        if (!chartData || !chartData.aggregatedData || chartData.aggregatedData.length === 0) {
            console.log('No chart data or empty aggregatedData, skipping chart rendering and clearing old charts.');
            clearChartContainer('overall-chart');

            return;
        }

        // Персональні графіки по сенсорах
        chartData.aggregatedData.forEach(sensor => {
            const containerId = `chart-${sensor.sensorId}`;
            const chartElement = clearChartContainer(containerId);

            if (!chartElement) {
                return;
            }

            const labels = sensor.data.map(entry => {
                const date = new Date(entry.timestamp || entry.createdAt);
                return `${date.getDate().toString().padStart(2, '0')}.${(date.getMonth() + 1)
                    .toString()
                    .padStart(2, '0')}`;
            });
            const values = sensor.data.map(entry => entry.value);

            console.log(`Rendering chart for Sensor ID ${sensor.sensorId}. Labels count: ${labels.length}, Values count: ${values.length}`);


            if (labels.length === 0 || values.length === 0) {
                console.warn(`No data for sensor ${sensor.sensorId}, skipping chart.`);
                chartElement.innerHTML = '<p style="text-align: center; padding-top: 20%;">Немає даних для цього сенсора.</p>';
                return;
            }

            try {
                new Chartist.Line(chartElement, {
                    labels,
                    series: [values],
                }, {
                    fullWidth: true,
                    chartPadding: { right: 40 },
                    axisX: {
                        showGrid: false,
                        labelInterpolationFnc: (value, index) => index % 3 === 0 ? value : null,
                    }
                });
            } catch (e) {
                console.error(`Error creating chart for sensor ${sensor.sensorId}:`, e);
                chartElement.innerHTML = '<p style="text-align: center; color: red; padding-top: 20%;">Помилка побудови графіка.</p>';
            }
        });

        const overallChartElement = clearChartContainer('overall-chart');
        if (!overallChartElement) {
            return;
        }

        const sensorTypesMap = {};
        chartData.aggregatedData.forEach(sensor => {
            sensor.data.forEach(entry => {
                const timeKey = new Date(entry.timestamp || entry.createdAt).toISOString().slice(0, 16);
                if (!sensorTypesMap[sensor.sensorType]) sensorTypesMap[sensor.sensorType] = {};
                if (!sensorTypesMap[sensor.sensorType][timeKey]) {
                    sensorTypesMap[sensor.sensorType][timeKey] = [];
                }
                sensorTypesMap[sensor.sensorType][timeKey].push(entry.value);
            });
        });

        const allTimestamps = Array.from(
            new Set(Object.values(sensorTypesMap).flatMap(typeData => Object.keys(typeData)))
        ).sort();

        const overallLabels = allTimestamps.map(ts => {
            const date = new Date(ts);
            return `${date.getDate().toString().padStart(2, '0')}.${(date.getMonth() + 1).toString().padStart(2, '0')}`;
        });

        const overallSeries = Object.keys(sensorTypesMap).map(sensorType => {
            return allTimestamps.map(ts => {
                const values = sensorTypesMap[sensorType][ts];
                if (!values || values.length === 0) return null;
                const avg = values.reduce((sum, val) => sum + val, 0) / values.length;
                return +avg.toFixed(2);
            });
        });

        console.log(`Rendering overall chart. Labels count: ${overallLabels.length}, Series count: ${overallSeries.length}`);


        if (overallLabels.length === 0 || overallSeries.length === 0 || overallSeries.every(s => s.length === 0 && s.every(val => val === null))) {
            console.warn('No data for overall chart, skipping.');
            overallChartElement.innerHTML = '<p style="text-align: center; padding-top: 20%;">Немає даних для загального графіка.</p>';
            return;
        }

        try {
            new Chartist.Line(overallChartElement, {
                labels: overallLabels,
                series: overallSeries,
            }, {
                fullWidth: true,
                chartPadding: { right: 40 },
                axisX: {
                    showGrid: false,
                    labelInterpolationFnc: (value, index) => index % 1 === 0 ? value : null,
                },
                lineSmooth: Chartist.Interpolation.cardinal({
                    tension: 0.2
                })
            });
        } catch (e) {
            console.error('Error creating overall chart:', e);
            overallChartElement.innerHTML = '<p style="text-align: center; color: red; padding-top: 20%;">Помилка побудови загального графіка.</p>';
        }

    }, [chartData]);

    const handleTimeRangeChange = (range) => {
        if (range !== timeRange && !isLoading) {
            setTimeRange(range);
        }
    };


    return (
        <div id="main-content" className="w-full flex-1 bg-gray-100 p-6 max-h-screen overflow-y-auto">
            <div className="max-w-7xl mx-auto">
                <div className="bg-white shadow rounded-lg p-4 md:p-6">
                    <div className="flex justify-between items-center border-b border-gray-200 pb-3 mb-4">
                        <h5 className="text-xl font-bold text-gray-900">
                            {id ? `${t('analytics_title')} ${id}` : t('field_not_selected')}
                        </h5>
                        <div className="flex space-x-2">
                            {['7d', '6m', '1y'].map(range => (
                                <button
                                    key={range}
                                    onClick={() => handleTimeRangeChange(range)}
                                    disabled={isLoading}
                                    className={`px-3 py-1 text-sm font-medium rounded transition-colors duration-150 ease-in-out ${
                                        timeRange === range
                                            ? 'bg-blue-600 text-white hover:bg-blue-700'
                                            : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                                    } ${isLoading ? 'opacity-50 cursor-not-allowed' : ''}`}
                                >
                                    {range.toUpperCase()}
                                </button>
                            ))}
                        </div>
                    </div>

                    {isLoading && <p className="text-blue-600">Завантаження даних...</p>}
                    {error && <p className="text-red-600">{error}</p>}

                    {chartData && !error && (
                        <>
                            <div className="mb-8">
                                <h6 className="font-semibold text-lg mb-2">{t(`general_analytics`)}</h6>
                                <div id="overall-chart" className="ct-chart ct-major-twelfth"></div>
                            </div>
                            <div>
                                <h6 className="font-semibold text-lg mb-2">{t(`aggregated_data`)}</h6>
                                <div className="space-y-4">
                                    {chartData.aggregatedData.map(sensor => (
                                        <div key={sensor.sensorId} className="p-4 bg-gray-50 rounded shadow-sm">
                                            <h6 className="font-bold">{sensor.sensorType}</h6>
                                            <p><strong>Min:</strong> {sensor.min}</p>
                                            <p><strong>Max:</strong> {sensor.max}</p>
                                            <p><strong>Avg:</strong> {sensor.average}</p>
                                            <div
                                                id={`chart-${sensor.sensorId}`}
                                                className="ct-chart ct-major-tenth mt-4"
                                                style={{ height: '300px' }}
                                            />
                                        </div>
                                    ))}
                                </div>
                            </div>
                        </>
                    )}

                    {id && !chartData && !error && !isLoading && <p>Немає даних для відображення.</p>}
                </div>
            </div>
        </div>
    );
};

export default AnalyticsField;