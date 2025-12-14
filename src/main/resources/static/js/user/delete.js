document.addEventListener("DOMContentLoaded", () => {
    const modal = document.querySelector('[data-modal-id="modalset-user-delete"]');
    const display = document.getElementById("deleteCodeDisplay");
    const input = document.getElementById("deleteCodeInput");
    const checkbox = document.getElementById("agreeDeleteAccount");
    const confirmBtn = document.getElementById("confirmDeleteBtn");
    const passwordInput = document.getElementById("deletePasswordInput");

    let randomCode = "";

    const generateRandomCode = () => {
        randomCode = Array.from(crypto.getRandomValues(new Uint8Array(20)))
            .map(b => "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"[b % 36])
            .join("");
        display.value = randomCode;
        input.value = "";
        passwordInput.value = "";
    };

    document.addEventListener("click", (e) => {
        const openBtn = e.target.closest(`[data-modal-target="modalset-user-delete"]`);
        if (openBtn) {
            generateRandomCode();
        }
    });

    confirmBtn.addEventListener("click", (e) => {
        const password = document.getElementById("deletePasswordInput").value;
        if (password === "") {
            alert("비밀번호를 입력해주세요.");
            return;
        }

        if (!checkbox.checked) {
            alert("회원 정보 삭제에 동의해야 진행할 수 있습니다.");
            return;
        }

        if (input.value.trim() !== randomCode) {
            alert("입력하신 코드가 일치하지 않습니다. 다시 확인해주세요.");
            return;
        }

        const confirmed = confirm("모든 회원 정보가 영구적으로 삭제됩니다.\n복구는 불가능합니다.\n정말 삭제하시겠습니까?");
        if (!confirmed) {
            alert("회원 탈퇴가 취소되었습니다.");
            return;
        }

        const csrfToken = document.querySelector('meta[name="_csrf"]').content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

        fetch('/myPage/user/delete', {
            method: 'POST',
            headers: {
                [csrfHeader]: csrfToken,
                'Content-Type': 'application/json'
            },
            credentials: 'same-origin',
            body: JSON.stringify({
                password: password
            })
        })
        .then(response => response.text())
        .then(result => {
            if (result !== "T") {
                alert(result);
                return;
            } else if (result === "T") {
                alert("Client 정보가 초기화 되었습니다. 로그아웃 됩니다.");
                fetch('/user/logout', {
                    method: 'POST',
                    headers: {
                        [csrfHeader]: csrfToken,
                        'Content-Type': 'application/x-www-form-urlencoded'
                    },
                    credentials: 'same-origin'
                }).then(() => {
                    window.location.href = "/";
                });
            }
        })
        .catch(err => {
            alert("회원 탈퇴 요청 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            window.location.href = "/";
        });
    });
});