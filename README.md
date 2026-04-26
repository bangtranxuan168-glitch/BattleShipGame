# Battleship Game - Hải Lực Thiết Thần

Chào mừng bạn đến với **Battleship Game** (Hải Lực Thiết Thần)! Đây là một trò chơi bắn tàu cổ điển được xây dựng bằng ngôn ngữ Java, sử dụng JavaFX cho giao diện đồ họa (GUI) và kiến trúc MVC.

---

## 1. Hướng dẫn tải code (Download)

Bạn có thể tải trực tiếp mã nguồn của dự án về máy tính theo 2 cách:

### Cách 1: Sử dụng Git (Khuyên dùng)
Nếu máy bạn đã cài đặt [Git](https://git-scm.com/), hãy mở Terminal hoặc Command Prompt và chạy lệnh sau:
```bash
git clone <địa-chỉ-repository-github-của-bạn>
cd BattleShipGame
```
*(Lưu ý: Thay `<địa-chỉ-repository-github-của-bạn>` bằng link Git của repo)*

### Cách 2: Tải file ZIP
1. Trên trang chủ của Repository trên GitHub, nhấn vào nút xanh **Code**.
2. Chọn **Download ZIP**.
3. Giải nén file vừa tải về và mở thư mục mã nguồn.

---

## 2. Hướng dẫn cài đặt và chạy code (Run)

### Yêu cầu hệ thống (Prerequisites)
Để chạy được trò chơi, máy tính của bạn cần cài đặt:
- **Java Development Kit (JDK)**: Phiên bản 17 trở lên.
- **Apache Maven**: Để quản lý thư viện và biên dịch dự án.

### Các bước chạy game

**Cách 1: Chạy trực tiếp qua Maven (JavaFX Plugin)**
Mở Terminal/Command Prompt tại thư mục gốc của dự án (nơi chứa file `pom.xml`) và chạy lệnh:
```bash
mvn clean javafx:run
```

**Cách 2: Build ra file JAR và chạy**
1. Biên dịch và đóng gói thành file JAR (có chứa sẵn thư viện):
```bash
mvn clean package
```
2. Sau khi build thành công, chạy file JAR trong thư mục `target`:
```bash
java -jar target/battleship-game-jar-with-dependencies.jar
```

---

## 3. Hướng dẫn chơi (How to Play)

Trò chơi tuân theo luật bắn tàu Battleship cổ điển:

### Giai đoạn 1: Đặt tàu (Placement Phase)
- Khi bắt đầu, bạn sẽ được cấp một số lượng tàu với các kích thước khác nhau.
- **Xoay tàu:** Sử dụng chuột (nhấp chuột phải) hoặc phím tắt (tuỳ vào giao diện hiển thị) để đổi chiều tàu ngang hoặc dọc.
- **Đặt tàu:** Nhấn chuột trái để đặt tàu lên bản đồ lưới của bạn. Bạn không thể đặt tàu chồng lên nhau hoặc ra ngoài vùng bản đồ.

### Giai đoạn 2: Tấn công (Attack Phase)
- Sau khi bạn đã đặt xong toàn bộ chiến hạm, trò chơi sẽ chuyển sang chế độ chiến đấu.
- Bạn và đối thủ (AI) sẽ lần lượt bắn vào bản đồ của nhau bằng cách nhấp chuột vào một ô vuông chưa bị bắn trên bản đồ của đối phương.
- Các biểu tượng sẽ hiện lên để thông báo:
  - 💥 **Hit (Trúng mục tiêu):** Bạn đã bắn trúng một phần của tàu địch. Hãy tiếp tục dò tìm xung quanh vị trí đó!
  - 🌊 **Miss (Trượt):** Không có tàu địch nào ở ô vuông đó.
- Nếu tất cả các phần của một chiếc tàu bị trúng đạn, tàu đó sẽ bị chìm (Sunk).

### Điều kiện thắng lợi
- Trò chơi kết thúc khi một bên đánh chìm được toàn bộ hạm đội tàu của đối phương. Ai là người làm được điều đó trước sẽ giành chiến thắng!

---

**Chúc bạn có những giờ phút giải trí vui vẻ với Battleship Game!**
