import React, { useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { Card, Button } from "antd";
import api from "../api/axios";

const PaymentResultPage = () => {
  const { search } = useLocation();
  const navigate = useNavigate();
  const query = new URLSearchParams(search);

  const status = query.get("status");
  const orderId = query.get("orderId");
  const amount = query.get("amount");

  useEffect(() => {
    if (status === "SUCCESS" && orderId) {
        api.post(`order-groups/${orderId}/pay`)
        .then(() => {
            console.log("✅ Order updated to PAID");
        })
        .catch((err) => {
            console.error("❌ Lỗi cập nhật order:", err);
        });
    }
    }, [status, orderId]);          

  return (
    <div style={{ display: "flex", justifyContent: "center", marginTop: 40 }}>
      <Card style={{ width: 400, textAlign: "center" }}>
        <h2>Kết quả thanh toán</h2>
        {status === "SUCCESS" ? (
          <div style={{ color: "green" }}>
            ✅ Thanh toán thành công!
            <p>Đơn hàng: {orderId}</p>
            <p>Số tiền: {(amount / 100).toLocaleString("vi-VN")} VND</p>
          </div>
        ) : (
          <div style={{ color: "red" }}>
            ❌ Thanh toán thất bại!
            <p>Đơn hàng: {orderId}</p>
          </div>
        )}
        <Button type="primary" onClick={() => navigate("/orders")}>
          Xem đơn hàng của tôi
        </Button>
      </Card>
    </div>
  );
};

export default PaymentResultPage;
