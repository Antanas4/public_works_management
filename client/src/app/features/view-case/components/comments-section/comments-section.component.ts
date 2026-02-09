import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Comment} from "../../../../core/models/comment.model";
import {CommentService} from "../../../../core/services/comment/comment.service";
import {ProcessingAction} from "../../../../core/models/processing-action.model";
import {EditableComment} from "../../../../core/models/editable-comment.model";

@Component({
    selector: 'app-comments-section',
    templateUrl: './comments-section.component.html',
    styleUrls: ['./comments-section.component.scss'],
    standalone: false
})
export class CommentsSectionComponent implements OnInit {
    @Input() caseId!: number;
    @Input() processingActions: ProcessingAction[] = [];
    @Output() dataUpdated: EventEmitter<void> = new EventEmitter<void>();

    comments: Comment[] = [];
    editableComment: EditableComment = {
        id: null,
        content: ''
    };
    newCommentContent: string = '';

    constructor(
        private readonly _commentService: CommentService,
    ) {}

    ngOnInit(): void {
        this.getComments();
    }

    startEdit(comment: Comment): void {
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
        if (!this.editableComment.content.trim()) return;

        const updatedComment = { ...comment, content: this.editableComment.content };

        this._commentService.updateComment(comment.id, updatedComment).subscribe(() => {
            this.cancelEdit();
            this.getComments();
            this.dataUpdated.emit();
        });
    }

    deleteComment(commentId: number): void {
        this._commentService.deleteComment(commentId).subscribe(() => {
            this.cancelEdit();
            this.getComments();
            this.dataUpdated.emit();
        });
    }

    addComment(): void {
        const content: string = this.newCommentContent.trim();

        const newComment = {
            content: content,
            caseId: this.caseId,
            userId: 1 // delete after user authentication logic added
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
