import React, { useEffect, useState } from "react";
import { Card, Button, message, Modal } from "antd";
import { useNavigate } from "react-router-dom";
import api from "../api/axios";

function OrderPage() {
  const [orders, setOrders] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    fetchOrders();
  }, []);

  const fetchOrders = () => {
    api
      .get("/order-groups")
      .then((res) => setOrders(res.data))
      .catch((err) => console.error("❌ Lỗi lấy orders:", err));
  };

  const formatVND = (amount) =>
    amount?.toLocaleString("vi-VN", { style: "currency", currency: "VND" });

  const handlePay = (order) => {
    Modal.confirm({
      title: "Xác nhận thanh toán",
      content: `Bạn có chắc chắn muốn thanh toán đơn hàng #${order.id} với số tiền ${formatVND(
        order.totalAmount
      )}?`,
      okText: "Xác nhận",
      cancelText: "Hủy",
      onOk: () => doPay(order),
    });
  };

  const doPay = async (order) => {
    if (order.payment === "VNPAY") {
      try {
        const res = await api.get(`/payment/create-payment?orderId=${order.id}`);
        if (res.data?.url) {
          window.location.href = res.data.url; // chuyển sang VNPay
        } else {
          message.error("Không tạo được link thanh toán VNPay");
        }
      } catch (err) {
        console.error("❌ Lỗi tạo payment VNPAY:", err);
        message.error("Lỗi khi tạo thanh toán VNPay!");
      }
    } else {
      try {
        await api.post(`/order-groups/${order.id}/pay`);
        message.success("Thanh toán thành công!");
        fetchOrders();

        navigate(
          `/payment-result?status=SUCCESS&orderId=${order.id}&amount=${order.totalAmount}`
        );
      } catch (error) {
        if (error.response) {
          const { code, message: backendMsg } = error.response.data;
          if (code === 5001) {
            message.error(
              "⚠️ Một trong các món ăn đã bị hủy, không thể thanh toán!"
            );
          } else {
            message.error(backendMsg || "Thanh toán thất bại!");
          }
        } else {
          message.error("Thanh toán thất bại! Lỗi kết nối server.");
        }

        navigate(
          `/payment-result?status=FAILED&orderId=${order.id}&amount=${order.totalAmount}`
        );
      }
    }
  };

  const handleCancel = (order) => {
    Modal.confirm({
      title: "Xác nhận hủy đơn",
      content: `Bạn có chắc chắn muốn hủy đơn hàng #${order.id}?`,
      okText: "Xác nhận",
      cancelText: "Quay lại",
      onOk: async () => {
        try {
          await api.post(`/order-groups/${order.id}/cancel`);
          message.success("Hủy đơn thành công!");
          fetchOrders();

          navigate(
            `/payment-result?status=FAILED&orderId=${order.id}&amount=${order.totalAmount}`
          );
        } catch {
          message.error("Hủy đơn thất bại!");
        }
      },
    });
  };

  return (
    <div style={{ padding: 24 }}>
      <h1 style={{ fontSize: 24, fontWeight: "bold", marginBottom: 16 }}>
        Danh sách đơn hàng
      </h1>
      {orders.length === 0 ? (
        <p>Không có đơn hàng nào</p>
      ) : (
        orders.map((order) => (
          <Card key={order.id} style={{ marginBottom: 16 }}>
            <p>
              <strong>ID:</strong> {order.id}
            </p>
            <p>
              <strong>Ngày tạo:</strong>{" "}
              {new Date(order.createdAt).toLocaleString()}
            </p>
            <p>
              <strong>Phương thức thanh toán:</strong> {order.payment}
            </p>
            <p>
              <strong>Trạng thái:</strong> {order.status}
            </p>
            <p>
              <strong>Tổng tiền:</strong> {formatVND(order.totalAmount)}
            </p>

            {order.status === "PENDING" && (
              <div style={{ marginTop: 12, display: "flex", gap: 8 }}>
                <Button type="primary" onClick={() => handlePay(order)}>
                  {order.payment === "VNPAY"
                    ? "Thanh toán VNPay"
                    : "Thanh toán"}
                </Button>
                <Button danger onClick={() => handleCancel(order)}>
                  Hủy
                </Button>
              </div>
            )}
          </Card>
        ))
      )}
    </div>
  );
}

export default OrderPage;
