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
	function updateBulkActionButton() {
		if (!btnBulkDelete) return;
		const checkedCount = document.querySelectorAll('.row-checkbox:checked').length;
		btnBulkDelete.disabled = checkedCount === 0;
	}
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
	clickableRows.forEach(row => {
		row.addEventListener('click', function(e) {
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
	rowCheckboxes.forEach(cb => {
		cb.addEventListener('change', function() {
			updateRowBackground(this);
			const total = rowCheckboxes.length;
			const checked = document.querySelectorAll('.row-checkbox:checked').length;
			if (selectAll) selectAll.checked = (total > 0 && total === checked);
			updateBulkActionButton();
		});
	});
	if (selectAll) {
		selectAll.addEventListener('change', function() {
			rowCheckboxes.forEach(cb => {
				cb.checked = this.checked;
				updateRowBackground(cb);
			});
			updateBulkActionButton();
		});
	}
	// ==========================================
	//一括削除ボタンのクリック制御
	// ==========================================
	if (btnBulkDelete) {
		btnBulkDelete.addEventListener('click', function(e) {
			const checkedCount = document.querySelectorAll('.row-checkbox:checked').length;
			let toastElement = document.getElementById("toastMessage");
			if (checkedCount === 0) {
				e.preventDefault();
				if (toastElement) {
					toastElement.className = "toast toast-error";
					toastElement.querySelector('span').textContent = "削除対象が選択されていません";
					toastElement.classList.add("show");
					setTimeout(function() {
						toastElement.classList.remove("show");
					}, 3000);
				}
				return;
			}
			openBulkDeleteModal();
		});
	}

	// ==========================================
	// ログアウトモーダル
	// ==========================================
	const logoutOverlay =
		document.getElementById('logoutOverlay');
	if (logoutOverlay) {
		logoutOverlay.addEventListener('click', function(e) {
			if (e.target === this) {
				closeLogoutModal();
			}
		});
	}
});
/**
 * ログアウトモーダル表示
 */
function openLogoutModal() {
	const overlay =
		document.getElementById('logoutOverlay');
	if (overlay) {
		overlay.style.display = 'flex';
	}
}
/**
 * ログアウトモーダル非表示
 */
function closeLogoutModal() {
	const overlay =
		document.getElementById('logoutOverlay');
	if (overlay) {
		overlay.style.display = 'none';
	}
}

// ==========================================
// 削除モーダル
// ==========================================
function openDeleteModal(id) {

	const overlay = document.getElementById('deleteOverlay');
	const form = document.getElementById('deleteForm');

	if (form) {
		form.action = '/accounts/' + id + '/delete';
	}

	if (overlay) {
		overlay.style.display = 'flex';
	}
}

function closeDeleteModal() {

	const overlay = document.getElementById('deleteOverlay');

	if (overlay) {
		overlay.style.display = 'none';
	}
}


// ==========================================
// 一括削除モーダル
// ==========================================
function openBulkDeleteModal() {

	const overlay = document.getElementById('bulkDeleteOverlay');

	if (overlay) {
		overlay.style.display = 'flex';
	}
}

function closeBulkDeleteModal() {

	const overlay = document.getElementById('bulkDeleteOverlay');

	if (overlay) {
		overlay.style.display = 'none';
	}
}

function submitBulkDelete() {
	const form = document.getElementById('listForm');

	if (form) {
		form.action = '/accounts/bulk-delete';
		form.submit();
	}
}