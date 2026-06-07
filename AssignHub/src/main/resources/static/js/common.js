/**
 * AssignHub 全画面共通のJavaScript
 */
document.addEventListener("DOMContentLoaded", function() {
    
    // ==========================================
    // 1. Toast通知の表示制御
    // ==========================================
    const toastElement = document.getElementById("toastMessage");
    if (toastElement && toastElement.textContent.trim() !== "") {
        toastElement.classList.add("show");
        setTimeout(function() {
            toastElement.classList.remove("show");
        }, 3000);
    }

    // ==========================================
    // 2. データテーブルの行選択・一括操作制御
    // ==========================================
    const selectAll = document.getElementById('selectAll');
    const rowCheckboxes = document.querySelectorAll('.row-checkbox');
    const btnBulkDelete = document.getElementById('btnBulkDelete');
    const clickableRows = document.querySelectorAll('.clickable-row');

    // 一括操作ボタンの活性/非活性を切り替え
    function updateBulkActionButton() {
        if (!btnBulkDelete) return;
        const checkedCount = document.querySelectorAll('.row-checkbox:checked').length;
        btnBulkDelete.disabled = checkedCount === 0;
    }

    // 行の背景色を更新
    function updateRowBackground(checkbox) {
        const tr = checkbox.closest('tr');
        if (tr && tr.classList.contains('clickable-row')) {
            if (checkbox.checked) {
                tr.classList.add('row-selected');
            } else {
                tr.classList.remove('row-selected');
            }
        }
    }

    // 行全体をクリックした時のイベント
    clickableRows.forEach(row => {
        row.addEventListener('click', function(e) {
            // チェックボックス自身、またはリンク/ボタンがクリックされた場合はスキップ
            if (e.target.type === 'checkbox' || e.target.closest('a') || e.target.closest('button')) {
                return;
            }
            const checkbox = this.querySelector('.row-checkbox');
            if (checkbox) {
                checkbox.checked = !checkbox.checked;
                checkbox.dispatchEvent(new Event('change'));
            }
        });
    });

    // 各チェックボックス変更時のイベント
    rowCheckboxes.forEach(cb => {
        cb.addEventListener('change', function() {
            updateRowBackground(this);
            const total = rowCheckboxes.length;
            const checked = document.querySelectorAll('.row-checkbox:checked').length;
            if (selectAll) selectAll.checked = (total > 0 && total === checked);
            updateBulkActionButton();
        });
    });

    // 「全選択」チェックボックスのイベント
    if (selectAll) {
        selectAll.addEventListener('change', function() {
            rowCheckboxes.forEach(cb => {
                cb.checked = this.checked;
                updateRowBackground(cb);
            });
            updateBulkActionButton();
        });
    }
});