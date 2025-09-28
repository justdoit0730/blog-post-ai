// 비밀번호 체크 함수 (조건별로 boolean 반환)
function checkPassword(pwd) {
    const trimmed = pwd.trim();
    const minLength = trimmed.length >= 8;
    const hasNumber = /\d/.test(trimmed);
    const hasSpecial = /[!@#\$%\^&\*\(\)_\+\-\=\[\]\{\};:'",<>\.?\/\\`~|]/.test(trimmed);
    const hasUpper = /[A-Z]/.test(trimmed);
    const hasLower = /[a-z]/.test(trimmed);

    let score = 0;
    [minLength, hasNumber, hasSpecial, hasUpper, hasLower].forEach(cond => { if(cond) score++; });

    let width = (score/5)*100;
    let color = score <= 2 ? 'red' : score === 3 ? 'orange' : 'green';
    const strengthBar = document.getElementById("pwStrengthBar");
    strengthBar.style.width = width + '%';
    strengthBar.style.background = color;

    const feedback = document.getElementById("pwFeedback");
    feedback.innerHTML = `
        <ul style="margin:0; display: flex; gap: 1rem;">
            <li>${minLength ? "✅" : "❌"} 8글자 이상</li>
            <li>${hasNumber ? "✅" : "❌"} 숫자 포함</li>
            <li>${hasSpecial ? "✅" : "❌"} 특수문자 포함</li>
            <li>${hasUpper ? "✅" : "❌"} 대문자 포함</li>
            <li>${hasLower ? "✅" : "❌"} 소문자 포함</li>
        </ul>
    `;

    const validOverall = minLength && hasNumber && hasSpecial && hasUpper && hasLower;

    return { validOverall, minLength, hasNumber, hasSpecial, hasUpper, hasLower };
}

document.getElementById("signUpAuthPassword").addEventListener('input', e => checkPassword(e.target.value));


// 저장
document.getElementById('passwordUpdate').addEventListener('click', function() {
    const passwordData = document.getElementById("signUpAuthPassword").value.trim();
    const passwordCheck = document.getElementById("signUpAuthPasswordCheck").value.trim();

    if (!passwordData) {
        alert("비밀번호를 입력해주세요.");
        document.getElementById("signUpAuthPassword").focus();
        return;
    }

    const pwCheck = checkPassword(passwordData);
    if (!pwCheck.validOverall) {
        alert("비밀번호가 요구 조건을 모두 충족하지 않습니다.\n- 8글자 이상\n- 숫자 포함\n- 특수문자 포함\n- 대문자 포함\n- 소문자 포함");
        document.getElementById("signUpAuthPassword").focus();
        return;
    }

    if (!passwordCheck) {
        alert("확인용 비밀번호를 입력해주세요.");
        document.getElementById("signUpAuthPasswordCheck").focus();
        return;
    }

    if (passwordData !== passwordCheck) {
        alert("비밀번호가 일치하지 않습니다. 다시 시도해주세요.");
        document.getElementById("signUpAuthPasswordCheck").focus();
        return;
    }

    const confirmReset = confirm("비밀번호 변경 시 로그아웃 됩니다. 정말 비밀번호를 변경하시겠습니까?");
    if (!confirmReset) return;

    var data = {
        password: passwordCheck
    };

    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    fetch('/password/update', {
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
        if (result === "T") {
            alert("비밀번호가 변경되었습니다. 로그아웃 됩니다.");
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
        } else {
            alert("비밀번호 저장 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    })
    .catch(err => {
        console.error(err);
        alert("비밀번호 저장 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    });
});
