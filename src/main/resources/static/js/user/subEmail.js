// 이메일 재작성
function resetEmailAuthUI() {
    const subEmail = document.getElementById("subEmail");
    const sendAuthBtn = document.getElementById("sendAuthBtn");

    subEmail.disabled = false;
    subEmail.value = '';
    sendAuthBtn.disabled = false;
}

document.getElementById("rewriteEmailBtn").addEventListener("click", function() {
    const confirmReset = confirm("다른 이메일을 저장하기 위해선 다시 인증을 시도해야만 합니다. 수신용 이메일을 초기화 하시겠습니까?");
    if (!confirmReset) return;

    clearInterval(timerInterval);
    document.querySelector("#countdown").textContent = "";
    resetEmailAuthUI();

    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    fetch("/subEmail/email/auth/clear", {
        method: "POST",
        headers: {
            [csrfHeader]: csrfToken,
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: new URLSearchParams({email}),
        credentials: 'same-origin'
    })
    .then()
    .catch(err => {
        console.error(err);
        alert("오류가 발생했습니다. 잠시 후에 다시 시도해주시길 바랍니다.");
        clearInterval(timerInterval);
        document.querySelector("#countdown").textContent = "";
    });

});


// 이메일 인증
let timerInterval;

// 이메일 인증 번호 보내기
document.querySelector("#sendAuthBtn").addEventListener("click", async function () {
    const email = document.getElementById("email").value;
    const subEmail = document.getElementById("subEmail").value;
    if (email === subEmail) {
        alert("수신용 이메일의 주소는 기존 이메일과 달라야합니다. ");
        return;
    }

     if (timerInterval) {
        const confirmResend = confirm("인증번호를 재전송 하시겠습니까?");
        if (!confirmResend) return;
        clearInterval(timerInterval);
        document.querySelector("#countdown").textContent = "";
    }
    startCountdown(5 * 60);

    const authInput = document.getElementById("signUpAuthCode");
    authInput.disabled = false;
    authInput.value = "";

    const verifyAuthBtn = document.getElementById("verifyAuthBtn");
    verifyAuthBtn.disabled = false;

    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
    alert("인증번호를 이메일로 전송했습니다.");
    fetch("/subEmail/authCode/send", {
        method: "POST",
        headers: {
            [csrfHeader]: csrfToken,
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: new URLSearchParams({subEmail}),
        credentials: 'same-origin'
    })
    .then(res => res.text())
    .then(result => {

        if (result && result.trim() !== "") {
            alert(result + ' 30분 동안 인증 시도는 5번 가능합니다.');
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

// 이메일 인증 번호 확인
document.querySelector("#verifyAuthBtn").addEventListener("click", async function () {
    const code = document.querySelector("#signUpAuthCode").value;

    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    const response = await fetch("/subEmail/authCode/verify", {
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

        const subEmailSave = document.querySelector("#subEmailSave");
        const subEmailUseRadio = document.getElementById("subEmailUseCheck");
        subEmailUseRadio.disabled = false;
        subEmailSave.disabled = false;
    }
});

// 인증번호 count
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

// 저장
document.getElementById('subEmailSave').addEventListener('click', function() {
    const email = document.getElementById("email").value.trim();
    const subEmail = document.getElementById("subEmail").value.trim();

    const selectedRadio = document.querySelector('input[name="flexRadioDefault"]:checked');
    const selected = selectedRadio ? selectedRadio.id : null;

    const isSubEmailUsed = document.getElementById("subEmailUseCheck").checked;

    if (!subEmail && isSubEmailUsed) {
        alert("수신 이메일이 비어있으면 선택할 수 없습니다.");
        return;
    }

    if (subEmail && subEmail === email) {
       alert("이메일과 다른 수신 이메일 주소를 작성해주세요.");
        return;
    }
    if (!selected) {
        alert("어떤 이메일을 수신용으로 사용할 지 선택해주세요.");
        return;
    }

    var data = {
        subEmail: subEmail,
        isSubEmailUsed: isSubEmailUsed
    };

    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    fetch('/subEmail/update', {
        method: 'POST',
        headers: {
            [csrfHeader]: csrfToken,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data),
        credentials: 'same-origin'
    })
    .then(response => response.text())
    .then(result => {
        if (result === "emailError") {
            alert("이메일과 다른 수신 이메일 주소를 작성해주세요.");
        } else if (result === "AuthError") {
            alert("이메일 인증이 완료되지 않았습니다.");
        } else if (result === "T") {
            alert("수신 이메일 저장이 완료되었습니다.");
            location.reload();
        } else {
            alert("수집 이메일 저장 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    })
    .catch(err => {
        console.error(err);
        alert("수집 이메일 저장 요청 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    });
});