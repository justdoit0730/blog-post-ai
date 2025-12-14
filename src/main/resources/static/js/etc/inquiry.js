document.getElementById('inquiryBtn').addEventListener('click', function() {

    const inquiryEmail = document.getElementById('inquiryEmail').value.trim();
    const inquiryTitle = document.getElementById('inquiryTitle').value.trim();
    const inquiryDetails = document.getElementById('inquiryDetails').value.trim();
    const agree = document.getElementById('checkset').checked;

    if (!inquiryEmail) {
        alert('이메일을 입력해주세요.');
        return;
    }

    if (!inquiryTitle) {
        alert('제목을 입력해주세요.');
        return;
    }

    if (!inquiryDetails) {
        alert('문의 내용을 입력해주세요.');
        return;
    }

    if (!agree) {
        alert('개인정보 수집동의에 체크해주세요.');
        return;
    }

    const inquiryDetailsHTML = inquiryDetails.replace(/\n/g, "<br>");

    var data = {
        inquiryEmail: inquiryEmail,
        inquiryTitle: inquiryTitle,
        inquiryDetails: inquiryDetailsHTML
    };

    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
    alert('문의 접수 완료까지 잠시만 기다려주세요.');

    fetch('/etc/community/inquery', {
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
        alert(result);
        window.location.href = "/etc/community/inquiry";
    })
    .catch(err => {
        console.error(err);
        alert("문의 처리 간 문제가 발생했습니다. 다시 시도해주세요.");
    });

});