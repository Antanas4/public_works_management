    import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
    import {Comment} from "../../../../core/models/comment.model";
    import {CommentService} from "../../../../core/services/comment/comment.service";
    import {ProcessingAction} from "../../../../core/models/processing-action.model";
    import {EditableComment} from "../../../../core/models/editable-comment.model";
    import {AuthService} from "../../../../core/services/auth/auth.service";

    @Component({
        selector: 'app-comments-section',
        templateUrl: './comments-section.component.html',
        styleUrls: ['./comments-section.component.scss'],
        standalone: false
    })
    export class CommentsSectionComponent implements OnInit {
        @Input() caseId!: number;
        @Input() processingActions: ProcessingAction[] = [];
        @Input() canAddComment: boolean = true;
        @Output() dataUpdated: EventEmitter<void> = new EventEmitter<void>();

        comments: Comment[] = [];
        editableComment: EditableComment = {
            id: null,
            content: ''
        };
        newCommentContent: string = '';
        currentUserId: number | null = null;

        constructor(
            private readonly _commentService: CommentService,
            private readonly _authService: AuthService,
        ) {}

        ngOnInit(): void {
            this.currentUserId = this._authService.getCurrentUser()?.id ?? null;
            this.getComments();
        }

        startEdit(comment: Comment): void {
            if (!this.canModifyComment(comment)) return;

            this.editableComment = {
                id: comment.id,
                content: comment.content
            };
        }

        cancelEdit(): void {
            this.editableComment = {
                id: null,
                content: ''
            };
        }

        saveEdit(comment: Comment): void {
            if (!this.canModifyComment(comment)) return;
            if (!this.editableComment.content.trim()) return;

            const updatedComment = { ...comment, content: this.editableComment.content };

            this._commentService.updateComment(comment.id, updatedComment).subscribe(() => {
                this.cancelEdit();
                this.getComments();
                this.dataUpdated.emit();
            });
        }

        deleteComment(commentId: number): void {
            const targetComment = this.comments.find(comment => comment.id === commentId);
            if (!targetComment || !this.canModifyComment(targetComment)) return;

            this._commentService.deleteComment(commentId).subscribe(() => {
                this.cancelEdit();
                this.getComments();
                this.dataUpdated.emit();
            });
        }

        canModifyComment(comment: Comment): boolean {
            return this.currentUserId !== null
                && comment?.userId !== undefined
                && comment.userId === this.currentUserId;
        }

        addComment(): void {
            const content: string = this.newCommentContent.trim();

            const newComment = {
                content: content,
                caseId: this.caseId,
            }

            this._commentService.addComment(newComment).subscribe(():void => {
                this.newCommentContent = '';
                this.getComments();
                this.dataUpdated.emit();
            })
        }

        isAIComment(commentId: number): boolean {
            if (!this.processingActions) {
                return;
            }

            for (const action of this.processingActions) {
                const params: Record<string, string> = action.parameters;
                const parsedId: number = Number(params.commentId);
                if (!Number.isNaN(parsedId) && parsedId === commentId && params.actionType === 'AI_COMMENT') {
                    return true;
                }
            }
            return false;
        }

        private getComments(): void {
            this._commentService.getCommentsByCaseId(this.caseId).subscribe(comments => {
                this.comments = comments;
            });
        }
    }
