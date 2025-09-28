document.getElementById('clientInfoCheck').addEventListener('click', function() {
    const clientId = document.getElementById('signUpClientId').value;
    const clientSecret = document.getElementById('signUpClientSecret').value;
    const isPrivacyAgreed = document.getElementById("isClientPrivacyAgreed").checked;

    if (!clientId || !clientSecret) {
        alert("값을 입력해주세요.");
        return;
    }

    const params = new URLSearchParams();
    params.append("clientId", clientId);
    params.append("clientSecret", clientSecret);
    params.append("isPrivacyAgreed", isPrivacyAgreed);

    const popup = window.open('', 'naverLoginPopup', "width=500,height=600,resizable=yes,scrollbars=yes,status=yes");

    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    fetch('/client/naverLoginPopup', {
        method: 'POST',
        body: params,
        headers: {
            [csrfHeader]: csrfToken,
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        credentials: 'same-origin',
        redirect: 'manual'
    })
    .then(response => response.text())
    .then(url => {
        popup.location.href = url;
    })
    .catch(err => {
        console.error(err);
        popup.close();
        alert('로그인 페이지를 열 수 없습니다.');
    });
});