// 削除確認モーダルを開く

function openDeleteModal(assignmentId) {
    const form = document.getElementById('deleteForm');
    form.action = '/assignments/' + assignmentId + '/delete';
    const overlay = document.getElementById('deleteOverlay');
    overlay.style.display = 'flex';
}
// 削除確認モーダルを閉じる

function closeDeleteModal() {
    document.getElementById('deleteOverlay').style.display = 'none';
}

// 背景クリックで削除モーダルを閉じる

document.getElementById('deleteOverlay').addEventListener('click', function(e) {
    if (e.target === this) closeDeleteModal();
});

// 一括削除確認モーダルを開く

function openBulkDeleteModal() {
    const checked = document.querySelectorAll('.row-checkbox:checked').length;
    if (checked === 0) {
        document.getElementById('noSelectionOverlay').style.display = 'flex';
        return;
    }
    document.getElementById('bulkDeleteOverlay').style.display = 'flex';
}

// 一括削除確認モーダルを閉じる

function closeBulkDeleteModal() {
    document.getElementById('bulkDeleteOverlay').style.display = 'none';
}

// 一括削除を実行する

function submitBulkDelete() {
    const form = document.getElementById('listForm');
    form.action = '/assignments/bulk-delete';
    form.method = 'post';
    form.submit();
}

// 未選択エラーモーダルを閉じる

function closeNoSelectionModal() {
    document.getElementById('noSelectionOverlay').style.display = 'none';
}

// 背景クリックで一括削除モーダルを閉じる

document.getElementById('bulkDeleteOverlay').addEventListener('click', function(e) {
    if (e.target === this) closeBulkDeleteModal();
});

// 背景クリックで未選択エラーモーダルを閉じる

document.getElementById('noSelectionOverlay').addEventListener('click', function(e) {
    if (e.target === this) closeNoSelectionModal();
});

document.addEventListener('DOMContentLoaded', function() {
    const searchForm = document.querySelector('.search-form');
    const startDateInput = document.getElementById('txt_contract_start_date');
    const endDateInput = document.getElementById('txt_contract_end_date');

    if (!searchForm || !startDateInput || !endDateInput) return;

    function showToastError(message) {
        let toast = document.getElementById('toast');

        if (!toast) {
            toast = document.createElement('div');
            toast.id = 'toast';
            toast.className = 'toast';
            toast.appendChild(document.createElement('span'));
            document.body.appendChild(toast);
        }

        const messageElement = toast.querySelector('span') || toast;
        messageElement.textContent = message;
        toast.classList.add('toast-error', 'show');

        setTimeout(function() {
            toast.classList.remove('show');
        }, 3000);
    }

    function clearDateErrors() {
        startDateInput.classList.remove('input-error');
        endDateInput.classList.remove('input-error');
    }

    searchForm.addEventListener('submit', function(e) {
        clearDateErrors();

        if (!startDateInput.validity.valid) {
            e.preventDefault();
            startDateInput.classList.add('input-error');
            showToastError('契約開始日は正しい日付を入力してください。');
            return;
        }

        if (!endDateInput.validity.valid) {
            e.preventDefault();
            endDateInput.classList.add('input-error');
            showToastError('契約終了日は正しい日付を入力してください。');
            return;
        }

        if (startDateInput.value && endDateInput.value && startDateInput.value > endDateInput.value) {
            e.preventDefault();
            startDateInput.classList.add('input-error');
            endDateInput.classList.add('input-error');
            showToastError('契約開始日は契約終了日以前の日付を入力してください。');
        }
    });
});
