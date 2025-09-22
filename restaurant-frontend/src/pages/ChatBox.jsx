// src/components/ChatBox.jsx
import React, { useState, useEffect } from "react";
import { Input, Button, Card, Spin } from "antd";
import { SendOutlined, MessageOutlined } from "@ant-design/icons";
import {
  collection,
  addDoc,
  serverTimestamp,
  query,
  orderBy,
  onSnapshot,
  where,
} from "firebase/firestore";
import { db } from "../firebase";
import api from "../api/axios";

export default function ChatBox({ restaurant }) {
  const [showChatBox, setShowChatBox] = useState(false);
  const [chatMessages, setChatMessages] = useState([]);
  const [chatInput, setChatInput] = useState("");
  const [chatLoading, setChatLoading] = useState(false);
  const [chatFetching, setChatFetching] = useState(false);
  const [currentUser, setCurrentUser] = useState(null);

  // lấy user hiện tại từ backend (token trong localStorage)
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

  // realtime listener: chỉ lắng nghe khi bật chat, có restaurant & user\
  useEffect(() => {
    if (!showChatBox || !restaurant?.id || !currentUser?.id) return;

    setChatFetching(true);

    // Query: lấy tất cả message của nhà hàng trước, sẽ filter client sau
    const q = query(
      collection(db, "messages"),
      where("restaurantId", "==", restaurant.id),
      orderBy("timestamp", "asc")
    );

    const unsubscribe = onSnapshot(
      q,
      (snapshot) => {
        const msgs = snapshot.docs
          .map((doc) => {
            const data = doc.data() || {};

            // normalize timestamp -> number (milliseconds)
            let ts = 0;
            const raw = data.timestamp;
            if (raw != null) {
              if (typeof raw === "number") {
                ts = raw;
              } else if (raw.toMillis && typeof raw.toMillis === "function") {
                ts = raw.toMillis();
              } else if (raw.seconds) {
                ts = raw.seconds * 1000 + (raw.nanoseconds || 0) / 1e6;
              } else {
                ts = Number(raw) || 0;
              }
            } else if (doc.createTime && doc.createTime.toMillis) {
              ts = doc.createTime.toMillis();
            } else {
              ts = Date.now();
            }

            return {
              id: doc.id,
              ...data,
              timestamp: ts,
            };
          })
          // filter: chỉ lấy tin nhắn giữa currentUser và owner
          .filter(
            (msg) =>
              (msg.senderId === currentUser.id && msg.receiverId === restaurant.ownerId) ||
              (msg.senderId === restaurant.ownerId && msg.receiverId === currentUser.id)
          )
          .sort((a, b) => (a.timestamp || 0) - (b.timestamp || 0));

        setChatMessages(msgs);
        setChatFetching(false);
      },
      (err) => {
        console.error("Snapshot error:", err);
        setChatFetching(false);
      }
    );

    return () => unsubscribe();
  }, [showChatBox, restaurant?.id, currentUser?.id, restaurant?.ownerId]);


  // gửi tin nhắn
  const handleSendMessage = async () => {
    if (!chatInput.trim() || !restaurant?.id || !currentUser?.id) return;

    setChatLoading(true);

    try {
      // Bạn có thể dùng serverTimestamp() nếu muốn đồng bộ timestamp phía server.
      // Nếu muốn tránh hiện tượng "null timestamp" khi local write, có thể gửi timestamp client (Date.now()) thay cho serverTimestamp()
      // 1) Dùng serverTimestamp (khuyến nghị) — UI sẽ được cập nhật khi snapshot về.
      await addDoc(collection(db, "messages"), {
        senderId: currentUser.id,
        senderName: currentUser.firstName && currentUser.lastName
          ? `${currentUser.firstName} ${currentUser.lastName}`
          : currentUser.fullName || currentUser.username || "Bạn",
        receiverId: restaurant.ownerId,
        restaurantId: restaurant.id,
        message: chatInput.trim(),
        timestamp: serverTimestamp(), // hoặc: Date.now()
      });

      // nếu muốn hiển thị ngay mà không chờ snapshot, có thể push 1 optimistic message:
      // setChatMessages(prev => [...prev, { id: 'local-'+Date.now(), senderId: currentUser.id, senderName: ..., receiverId: restaurant.ownerId, restaurantId: restaurant.id, message: chatInput.trim(), timestamp: Date.now(), _local: true }]);

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
          style={{ maxWidth: 420 }}
        >
          <div
            style={{
              border: "1px solid #eee",
              borderRadius: 8,
              padding: 12,
              height: 300,
              overflowY: "auto",
              marginBottom: 12,
              background: "#fff",
            }}
          >
            {chatFetching ? (
              <div style={{ textAlign: "center", paddingTop: 40 }}>
                <Spin />
              </div>
            ) : chatMessages.length === 0 ? (
              <div style={{ color: "#888", textAlign: "center", paddingTop: 20 }}>
                Chưa có tin nhắn
              </div>
            ) : (
              chatMessages.map((msg) => {
                const isMe = msg.senderId === currentUser?.id;
                return (
                  <div
                    key={msg.id}
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
                        maxWidth: "78%",
                        wordBreak: "break-word",
                        boxShadow: isMe ? "0 1px 3px rgba(22,119,255,0.2)" : "none",
                      }}
                    >
                      {!isMe && (
                        <div style={{ fontSize: 12, marginBottom: 6, color: "#444" }}>
                          {msg.senderName}
                        </div>
                      )}
                      <div>{msg.message}</div>
                      <div style={{ fontSize: 11, color: "#777", marginTop: 6, textAlign: isMe ? "right" : "left" }}>
                        {msg.timestamp ? new Date(msg.timestamp).toLocaleTimeString() : ""}
                      </div>
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
