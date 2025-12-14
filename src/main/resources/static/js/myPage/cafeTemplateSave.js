document.getElementById('cafeIdTemplateSave').addEventListener('click', function() {
    const tableBody = document.querySelector("#dynamicIdTable tbody");
    const rows = [...tableBody.rows];
    const templates = [];

    for (let index = 0; index < rows.length; index++) {
        const row = rows[index];
        const isUsed = row.cells[0].querySelector("input[type='radio']").checked;
        const no = index + 1;
        const cafeName = row.cells[2].textContent.trim();
        const cafeId = row.cells[3].textContent.trim();

        if (templates.some(t => t.cafeName === cafeName)) {
            alert(`중복된 카페 이름 값이 있습니다: ${cafeName}`);
            return;
        }

        if (templates.some(t => t.cafeId === cafeId)) {
            alert(`중복된 카페 ID 값이 있습니다: ${cafeId}`);
            return;
        }

        templates.push({
            no: no,
            isUsed: isUsed,
            cafeName: cafeName,
            cafeId: cafeId
        });
    }
    const templateJson = JSON.stringify(templates);

    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    fetch('/cafeId/template/save', {
        method: 'POST',
        headers: {
            [csrfHeader]: csrfToken,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            template: templateJson
        })
    })
    .then(res => res.text())
    .then(result => {
        if (result === "T") {
            alert("Cafe ID Template 정보가 저장되었습니다.");
        } else if (result === "D-C-F001") {
             alert("이메일 인증이 완료되지 않았습니다. 재 로그인 혹은 이메일 인증을 다시 진행해주세요.");
        } else if (result === "MAX_ROW") {
           alert("템플릿 최대 개수를 초과하였습니다. 최대 10개 까지 생성 가능합니다.");
        } else {
            alert("Cafe ID Template 정보 저장 요청 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요: " + result);
        }
    })
    .catch(err => {
        alert("Cafe ID Template 저장 요청 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요");
    });
});

document.getElementById('cafePostingTemplateSave').addEventListener('click', function() {
    const tableBody = document.querySelector("#dynamicPostingTable tbody");
    const rows = [...tableBody.rows];
    const templates = [];

    for (let index = 0; index < rows.length; index++) {
        const row = rows[index];
        const isUsed = row.cells[0].querySelector("input[type='radio']").checked;
        const no = index + 1;
        const tag = row.cells[2].textContent.trim();
        const cafeMenuId = row.cells[3].textContent.trim();
        const subject = row.cells[4].textContent.trim();
        const prompt = row.cells[5].textContent.trim();

        if (templates.some(t => t.tag === tag)) {
            alert(`중복된 태그 값이 있습니다: ${tag}`);
            return;
        }

        templates.push({
            no: no,
            isUsed: isUsed,
            tag: tag,
            cafeMenuId: cafeMenuId,
            subject: subject,
            prompt: prompt
        });
    }
    const templateJson = JSON.stringify(templates);

    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    fetch('/cafePosing/template/save', {
        method: 'POST',
        headers: {
            [csrfHeader]: csrfToken,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            template: templateJson
        })
    })
    .then(res => res.text())
    .then(result => {
        if (result === "T") {
            alert("Cafe Posting Template 정보가 저장되었습니다.");
        } else if (result === "D-C-F001") {
             alert("이메일 인증이 완료되지 않았습니다. 재 로그인 혹은 이메일 인증을 다시 진행해주세요.");
        } else if (result === "MAX_ROW") {
           alert("템플릿 최대 개수를 초과하였습니다. 최대 10개 까지 생성 가능합니다.");
        } else {
            alert("Cafe Posing Template 정보 저장 요청 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요: " + result);
        }
    })
    .catch(err => {
        alert("Cafe Posing Template 저장 요청 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요");
    });
});




