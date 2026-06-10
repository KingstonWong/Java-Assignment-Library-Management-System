using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace Library_Management_API.Migrations
{
    /// <inheritdoc />
    public partial class _09 : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.CreateTable(
                name: "BookReviewTable",
                columns: table => new
                {
                    review_id = table.Column<int>(type: "int", nullable: false)
                        .Annotation("SqlServer:Identity", "1, 1"),
                    book_id = table.Column<int>(type: "int", nullable: false),
                    member_id = table.Column<int>(type: "int", nullable: false),
                    rating = table.Column<int>(type: "int", nullable: false),
                    comment = table.Column<string>(type: "nvarchar(max)", nullable: false),
                    created_at = table.Column<DateOnly>(type: "date", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_BookReviewTable", x => x.review_id);
                    table.ForeignKey(
                        name: "FK_BookReviewTable_BookTable_book_id",
                        column: x => x.book_id,
                        principalTable: "BookTable",
                        principalColumn: "book_id",
                        onDelete: ReferentialAction.Cascade);
                    table.ForeignKey(
                        name: "FK_BookReviewTable_MemberTable_member_id",
                        column: x => x.member_id,
                        principalTable: "MemberTable",
                        principalColumn: "member_id",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.CreateIndex(
                name: "IX_BookReviewTable_book_id",
                table: "BookReviewTable",
                column: "book_id");

            migrationBuilder.CreateIndex(
                name: "IX_BookReviewTable_member_id",
                table: "BookReviewTable",
                column: "member_id");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropTable(
                name: "BookReviewTable");
        }
    }
}
