document.getElementById('aiWriteInfoSave').addEventListener('click', function() {
    const maxToken = document.getElementById("maxToken").value.trim();
    const temperature = document.getElementById("temperature").value.trim();
    const selectedBtn = document.getElementById("selectTextVolume").textContent;

    if (maxToken < 100) {
        alert("Max Token 값은 100 이상 부터 설정 가능합니다.");
        return;
    }

    let textVolume = '1';
    let tempStr = document.getElementById("temperature").value.trim();
    let tempNum = parseInt(tempStr);

    if (isNaN(tempNum)) {
        tempNum = 50;
    }

    let tempValue = Math.ceil((tempNum / 100) * 10) / 10;

    if (tempValue > 1.0) {
        tempValue = 1.0;
    }

    if (selectedBtn.includes("중간")) {
        textVolume = '2';
    } else if (selectedBtn.includes("긴")) {
        textVolume = '3';
    }

    var data = {
        maxToken: maxToken,
        temperature: tempValue,
        textVolume: textVolume
    };

    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    fetch('/ai/setting/save', {
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
        if (result === "isPrivacyAgreedError") {
            alert("개인정보 수집 및 이용에 동의해야 합니다.");
        } else if (result === "emailAuthError") {
            alert("이메일 인증이 완료되지 않았습니다.");
        } else if (result === "clientAuthError") {
            alert("클라이언트 인증이 완료되지 않았습니다.");
        } else if (result === "T") {
            alert("AI 설정이 저장 되었습니다!");
        } else {
            alert("AI 설정 저장 요청 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    })
    .catch(err => {
        console.error(err);
        alert("AI 설정 저장 요청 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    });

});