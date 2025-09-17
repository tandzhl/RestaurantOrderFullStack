import React, { useState, useEffect } from "react";
import { Input, Button, Card } from "antd";
import { SendOutlined, MessageOutlined } from "@ant-design/icons";
import api from "../api/axios";
import "../App.css";

export default function ChatBox({ restaurant }) {
  const [showChatBox, setShowChatBox] = useState(false);

  const [chatMessages, setChatMessages] = useState([]);
  const [chatInput, setChatInput] = useState("");
  const [chatLoading, setChatLoading] = useState(false);
  const [chatFetching, setChatFetching] = useState(false);
  const [currentUser, setCurrentUser] = useState(null);

  // 📌 Lấy thông tin user
  useEffect(() => {
    const fetchUser = async () => {
      try {
        const res = await api.get("/users/me");
        setCurrentUser(res.data);
      } catch (err) {
        console.error("Không lấy được user:", err);
      }
    };
    fetchUser();
  }, []);

  // 📌 Lấy tin nhắn khi mở chat
  useEffect(() => {
    if (!showChatBox) return;
    if (!restaurant?.id) return;

    const fetchChat = async () => {
      setChatFetching(true);
      try {
        const res = await api.get(`/chat/user/${restaurant.id}/messages`);
        setChatMessages(Array.isArray(res.data) ? res.data : []);
      } catch (err) {
        console.error("Không load được tin nhắn:", err);
      } finally {
        setChatFetching(false);
      }
    };

    fetchChat();
  }, [showChatBox, restaurant?.id]);

  // 📌 Gửi tin nhắn
  const handleSendMessage = async () => {
    if (!chatInput.trim()) return;
    if (!restaurant?.id) return;

    setChatLoading(true);
    try {
      // Gửi dạng query params thay vì FormData
      await api.post("/chat/user/send", null, {
        params: {
          restaurantId: restaurant.id,
          message: chatInput,
        },
      });

      // append tin nhắn mới vào UI
      setChatMessages((prev) => [
        ...prev,
        {
          senderId: currentUser?.id,
          senderName: currentUser?.fullName || "Bạn",
          message: chatInput,
          timestamp: Date.now(),
        },
      ]);
      setChatInput("");
    } catch (err) {
      console.error("Không gửi được tin nhắn:", err);
    } finally {
      setChatLoading(false);
    }
  };

  return (
    <div style={{ marginTop: 20 }}>
      {!showChatBox ? (
        <Button
          type="default"
          icon={<MessageOutlined />}
          onClick={() => setShowChatBox(true)}
        >
          Liên hệ với cửa hàng
        </Button>
      ) : (
        <Card
          title={`Chat với ${restaurant?.name || "nhà hàng"}`}
          extra={
            <Button type="link" onClick={() => setShowChatBox(false)}>
              Đóng
            </Button>
          }
          style={{ maxWidth: 400 }}
        >
          <div
            className="chat-messages"
            style={{
              border: "1px solid #ddd",
              borderRadius: 8,
              padding: 12,
              maxHeight: 250,
              overflowY: "auto",
              marginBottom: 12,
            }}
          >
            {chatFetching ? (
              <p>Đang tải tin nhắn...</p>
            ) : (
              chatMessages.map((msg, i) => {
                const isMe = msg.senderId === currentUser?.id;
                return (
                  <div
                    key={i}
                    style={{
                      display: "flex",
                      justifyContent: isMe ? "flex-end" : "flex-start",
                      marginBottom: 8,
                    }}
                  >
                    <div
                      style={{
                        background: isMe ? "#1677ff" : "#f0f0f0",
                        color: isMe ? "#fff" : "#000",
                        padding: "8px 12px",
                        borderRadius: 16,
                        maxWidth: "70%",
                        wordBreak: "break-word",
                      }}
                    >
                      {!isMe && (
                        <div style={{ fontSize: 12, marginBottom: 4 }}>
                          {msg.senderName}
                        </div>
                      )}
                      {msg.message}
                    </div>
                  </div>
                );
              })
            )}
          </div>

          <div style={{ display: "flex", gap: 8 }}>
            <Input
              placeholder="Nhập tin nhắn..."
              value={chatInput}
              onChange={(e) => setChatInput(e.target.value)}
              onPressEnter={handleSendMessage}
              disabled={chatLoading}
            />
            <Button
              type="primary"
              icon={<SendOutlined />}
              onClick={handleSendMessage}
              loading={chatLoading}
            >
              Gửi
            </Button>
          </div>
        </Card>
      )}
    </div>
  );
}
