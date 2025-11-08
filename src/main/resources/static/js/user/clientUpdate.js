// client 정보 저장
document.getElementById('clientInfoSave').addEventListener('click', function() {
    const clientId = document.getElementById("signUpClientId").value.trim();
    const clientSecret = document.getElementById("signUpClientSecret").value.trim();
    const isPrivacyAgreed = document.getElementById("isClientPrivacyAgreed").checked;

    var data = {
        clientId: clientId,
        clientSecret: clientSecret,
        isPrivacyAgreed: isPrivacyAgreed
    };

    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    fetch('/myPage/client/update', {
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
        if (result === "D-C-F001") {
            alert("이메일 인증이 완료되지 않았습니다. 재 로그인 혹은 이메일 인증을 다시 진행해주세요.");
        } else if (result === "D-C-F002") {
            alert("클라이언트 인증이 완료되지 않았습니다. Client 인증을 다시 진행해주세요.");
        } else if (result === "D-C-F003") {
             alert("개인정보 수집동의 체크 후 다시 설정 실행 해주세요.");
        } else if (result === "T") {
            alert("Client 정보가 저장되었습니다.");
            window.location.href = "/myPage/postingSetting";
        } else {
            alert("저장 요청 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    })
    .catch(err => {
        console.error(err);
        alert("저장 요청 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    });
});


document.getElementById('clientInfoClear').addEventListener('click', function() {

    const confirmed = confirm(
        "삭제 후에는 Client 인증 전 까지 Cafe Posting 관련 모든 기능을 수행할 수 없게 됩니다.\n" +
        "정상 삭제처리 후 로그아웃 됩니다.\n\n" +
        "정말로 Client 정보를 삭제하시겠습니까?"
    );

    if (!confirmed) {
        event.preventDefault();
        return;
    }

    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    fetch('/myPage/client/clear', {
        method: 'POST',
        headers: {
            [csrfHeader]: csrfToken,
            'Content-Type': 'application/json'
        },
        credentials: 'same-origin'
    })
    .then(response => response.text())
    .then(result => {
    console.log(result);
        if (result === "D-C-F001") {
            alert("이메일 인증이 완료되지 않았습니다. 재 로그인 혹은 이메일 인증을 다시 진행해주세요.");
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
        } else {
            alert("저장 요청 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    })
    .catch(err => {
        console.error(err);
        alert("저장 요청 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    });
});