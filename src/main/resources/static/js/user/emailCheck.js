// 이메일 중복 검사
function checkEmailDup() {
    const emailInput = document.querySelector("#signUpLogin").value.trim();
    if (!emailInput) {
        alert("이메일을 입력해주세요.");
        return;
    }

    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailPattern.test(emailInput)) {
        alert("이메일 형식이 올바르지 않습니다.");
        return;
    }

    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    $.ajax({
        type: 'POST',
        url: '/user/authCode/email',
        data: { email: emailInput },
        beforeSend: function(xhr) {
            xhr.setRequestHeader(csrfHeader, csrfToken);
        },
        success: function(response) {
            if (response === true || response === "true") {
                alert('사용 가능한 이메일 입니다. 인증번호를 입력해 이메일을 인증하세요.');

                const checkBtn = document.getElementById("checkEmailDupBtn");
                checkBtn.style.background = "royalblue";
                checkBtn.style.pointerEvents = "none";
                checkBtn.style.cursor = "default";

                document.getElementById("signUpLogin").disabled = true;

                const rewriteBtn = document.getElementById("rewriteEmailBtn");
                const sendAuthBtn = document.getElementById("sendAuthBtn");
                const verifyBtn = document.getElementById("verifyAuthBtn");

                [rewriteBtn, sendAuthBtn, verifyBtn].forEach(btn => {
                    btn.style.background = "";
                    btn.style.pointerEvents = "";
                    btn.style.cursor = "pointer";
                });

                document.getElementById("signUpAuthCode").disabled = false;

            } else if (response === false || response === "false") {
                alert('이미 사용 중인 이메일입니다. 다른 이메일을 작성해 주세요.');
            }
        },
        error: function(xhr, status, error) {
            alert('회원가입에 실패하였습니다. 다시 시도하거나 관리자에게 문의 바랍니다.');
        }
    });
}

document.getElementById("checkEmailDupBtn").addEventListener("click", checkEmailDup);

// 이메일 재작성
function resetEmailAuthUI() {
    const emailInput = document.getElementById("signUpLogin");
    emailInput.disabled = false;
    emailInput.value = "";

    const authInput = document.getElementById("signUpAuthCode");
    authInput.disabled = true;
    authInput.value = "";

    const checkBtn = document.getElementById("checkEmailDupBtn");
    const rewriteBtn = document.getElementById("rewriteEmailBtn");
    const sendAuthBtn = document.getElementById("sendAuthBtn");
    const verifyBtn = document.getElementById("verifyAuthBtn");

    checkBtn.style.background = "";
    checkBtn.style.pointerEvents = "";
    checkBtn.style.cursor = "pointer";

    [rewriteBtn, sendAuthBtn, verifyBtn].forEach(btn => {
        btn.style.background = "darkgray";
        btn.style.pointerEvents = "none";
        btn.style.cursor = "default";
    });
}

document.getElementById("rewriteEmailBtn").addEventListener("click", function() {
    const confirmReset = confirm("이메일 재작성을 위한 중복 검사를 다시 수행하시겠습니까?");
    if (confirmReset) {
        clearInterval(timerInterval);
        document.querySelector("#countdown").textContent = "";
        resetEmailAuthUI();
    }
});

// 이메일 인증
let timerInterval;

// 이메일 인증 번호 보내기
document.querySelector("#sendAuthBtn").addEventListener("click", async function () {
    const email = document.querySelector("#signUpLogin").value;
    if (!email) {
        alert("이메일을 입력해주세요.");
        return;
    }

     if (timerInterval) {
        const confirmResend = confirm("인증번호를 재전송 하시겠습니까?");
        if (!confirmResend) return;
        clearInterval(timerInterval);
        document.querySelector("#countdown").textContent = "";
    }

    alert("인증번호를 이메일로 전송했습니다.");
    startCountdown(5 * 60);

    const verifyBtn = document.querySelector("#verifyAuthBtn");
    verifyBtn.style.background = "";
    verifyBtn.style.pointerEvents = "";
    verifyBtn.style.cursor = "pointer";

    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    fetch("/user/authCode/send", {
        method: "POST",
        headers: {
            [csrfHeader]: csrfToken,
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: new URLSearchParams({email}),
        credentials: 'same-origin'
    })
    .then(res => res.text())
    .then(result => {
        if (result && result.trim() !== "") {
            alert(text + ' 30분 동안 인증 시도는 5번 가능합니다.');
            return;
        }
    })
    .catch(err => {
        console.error(err);
        alert("인증번호 전송 중 오류가 발생했습니다.");
        clearInterval(timerInterval);
        document.querySelector("#countdown").textContent = "";
    });
});

function startCountdown(duration) {
    clearInterval(timerInterval);
    let time = duration;
    const display = document.querySelector("#countdown");

    timerInterval = setInterval(() => {
        let minutes = String(Math.floor(time / 60)).padStart(2, '0');
        let seconds = String(time % 60).padStart(2, '0');
        display.textContent = `${minutes}:${seconds}`;

        if (--time < 0) {
            clearInterval(timerInterval);
            display.textContent = "만료됨";
        }
    }, 1000);
}

// 이메일 인증 번호 확인
document.querySelector("#verifyAuthBtn").addEventListener("click", async function () {
    const code = document.querySelector("#signUpAuthCode").value;

    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    const response = await fetch("/user/authCode/verify", {
        method: "POST",
        headers: {
            [csrfHeader]: csrfToken,
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: new URLSearchParams({code}),
        credentials: 'same-origin'
    });

    const text = await response.text();
    alert(text);

    if (text.includes("인증 성공")) {
        clearInterval(timerInterval);
        document.querySelector("#countdown").textContent = "";

        const sendBtn = document.querySelector("#sendAuthBtn");
        const verifyBtn = document.querySelector("#verifyAuthBtn");
        const emailInput = document.querySelector("#signUpLogin");
        const authInput = document.querySelector("#signUpAuthCode");

        sendBtn.style.background = "darkgray";
        sendBtn.style.pointerEvents = "none";
        sendBtn.style.cursor = "default";

        verifyBtn.style.background = "royalblue";
        verifyBtn.style.pointerEvents = "none";
        verifyBtn.style.cursor = "default";

        emailInput.disabled = true;
        authInput.disabled = true;
    }
});
