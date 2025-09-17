import { useState } from "react"; // ❌ bỏ useEffect
import { Tabs, DatePicker, Card, Spin, message } from "antd";
import { Bar } from "react-chartjs-2";
import api from "../api/axios";
import dayjs from "dayjs";

import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  BarElement,
  LineElement,
  CategoryScale,
  LinearScale,
  PointElement,
} from "chart.js";
import { useParams } from "react-router-dom";

ChartJS.register(
  Title,
  Tooltip,
  Legend,
  BarElement,
  LineElement,
  CategoryScale,
  LinearScale,
  PointElement
);

const { TabPane } = Tabs;

function RestaurantRevenueReport() {
  const { id } = useParams(); 
  const restaurantId = parseInt(id, 10);
  const [loading, setLoading] = useState(false);
  const [month, setMonth] = useState(dayjs());
  const [year, setYear] = useState(dayjs());
  const [data, setData] = useState({ labels: [], revenues: [], orders: [] });

  const fetchRevenueMonth = async () => {
    try {
      setLoading(true);
      const res = await api.get(
        `/reports/revenue/weeks/${restaurantId}?year=${month.year()}&month=${
          month.month() + 1
        }`
      );
      const labels = res.data.map((item) => `Tuần ${item.week}`);
      const revenues = res.data.map((item) => item.totalRevenue);
      const orders = res.data.map((item) => item.successOrders);
      setData({ labels, revenues, orders });
    } catch {
      message.error("Không tải được dữ liệu doanh thu tháng");
    } finally {
      setLoading(false);
    }
  };

  const fetchRevenueYear = async () => {
    try {
      setLoading(true);
      const res = await api.get(`/reports/revenue/month/${restaurantId}`);
      const filtered = res.data.filter((item) => item.year === year.year());
      const labels = filtered.map((item) => `Tháng ${item.month}`);
      const revenues = filtered.map((item) => item.totalRevenue);
      const orders = filtered.map((item) => item.successOrders);
      setData({ labels, revenues, orders });
    } catch {
      message.error("Không tải được dữ liệu doanh thu năm");
    } finally {
      setLoading(false);
    }
  };

  const chartData = {
    labels: data.labels,
    datasets: [
        {
        type: "bar",
        label: "Doanh thu (VNĐ)",
        data: data.revenues,
        backgroundColor: "rgba(75, 192, 192, 0.5)",
        yAxisID: "y1",
        },
        {
        type: "line",
        label: "Số đơn hàng",
        data: data.orders,
        borderColor: "rgba(255, 99, 132, 1)",
        borderWidth: 2,
        fill: false,
        tension: 0.3,
        yAxisID: "y2",
        },
    ],
    };

    const options = {
    responsive: true,
    interaction: { mode: "index", intersect: false },
    plugins: {
        legend: { position: "top" },
    },
    scales: {
        y1: {
        type: "linear",
        position: "left",
        beginAtZero: true,
        title: { display: true, text: "Doanh thu (VNĐ)" },
        },
        y2: {
        type: "linear",
        position: "right",
        beginAtZero: true,
        grid: { drawOnChartArea: false }, // tránh chồng lưới
        title: { display: true, text: "Số đơn hàng" },
        },
    },
    };

  return (
    <Card style={{ margin: 24 }}>
      <h2 className="text-xl font-bold mb-4">Báo cáo doanh thu</h2>
      <Tabs defaultActiveKey="month" destroyInactiveTabPane>
        {/* Tab doanh thu tháng */}
        <TabPane tab="Doanh thu tháng" key="month">
          <DatePicker
            picker="month"
            value={month}
            onChange={(val) => setMonth(val)}
            style={{ marginBottom: 16 }}
          />
          <button
            onClick={fetchRevenueMonth}
            style={{
              marginLeft: 8,
              padding: "4px 12px",
              border: "1px solid #1890ff",
              borderRadius: 6,
              color: "#1890ff",
              background: "white",
              cursor: "pointer",
            }}
          >
            Xem báo cáo
          </button>
          {loading ? <Spin /> : <Bar data={chartData} options={options} />}
        </TabPane>

        {/* Tab doanh thu năm */}
        <TabPane tab="Doanh thu năm" key="year">
          <DatePicker
            picker="year"
            value={year}
            onChange={(val) => setYear(val)}
            style={{ marginBottom: 16 }}
          />
          <button
            onClick={fetchRevenueYear}
            style={{
              marginLeft: 8,
              padding: "4px 12px",
              border: "1px solid #1890ff",
              borderRadius: 6,
              color: "#1890ff",
              background: "white",
              cursor: "pointer",
            }}
          >
            Xem báo cáo
          </button>
          {loading ? <Spin /> : <Bar data={chartData} options={options} />}
        </TabPane>
      </Tabs>
    </Card>
  );
}

export default RestaurantRevenueReport;
